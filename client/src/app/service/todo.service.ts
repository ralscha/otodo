import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {AppDatabase} from '../model/app-database';
import {BehaviorSubject, lastValueFrom, Observable} from 'rxjs';
import {Todo} from '../model/todo';
import {TodoSyncRequest} from '../model/todo-sync-request';
import {TodoSyncResponse} from '../model/todo-sync-response';

@Injectable({
  providedIn: 'root'
})
export class TodoService {

  private readonly todosSubject = new BehaviorSubject<Todo[]>([]);
  private readonly todos$ = this.todosSubject.asObservable();

  constructor(private readonly httpClient: HttpClient,
              private readonly appDatabase: AppDatabase) {
  }

  private static changed(oldTodo: Todo | undefined, newTodo: Todo): boolean {
    if (oldTodo !== undefined && oldTodo.subject !== newTodo.subject) {
      return true;
    }
    return oldTodo !== undefined && oldTodo.description !== newTodo.description;

  }

  getTodos(): Observable<Todo[]> {
    this.updateSubject();
    return this.todos$;
  }

  getTodo(id: number): Promise<Todo | undefined> {
    return this.appDatabase.todos.get(id);
  }

  async delete(todo: Todo): Promise<void> {
    todo.ts = -1;
    await this.appDatabase.todos.put(todo);
    this.requestSync().catch(e => console.log(e));
  }

  async save(todo: Todo): Promise<void> {
    if (!todo.id) {
      todo.id = await this.getNextNewId();
      todo.ts = Math.floor(Date.now() / 1000);
      await this.appDatabase.todos.add(todo);
      this.requestSync().catch(e => console.log(e));
    } else {
      const oldTodo = await this.appDatabase.todos.get(todo.id);
      if (TodoService.changed(oldTodo, todo)) {
        todo.ts = Math.floor(Date.now() / 1000);
        await this.appDatabase.todos.put(todo);
        this.requestSync().catch(e => console.log(e));
      }
    }
  }

  async requestSync(): Promise<void> {
    this.updateSubject();

    const syncViewObject = await lastValueFrom(this.httpClient.get<{ [key: string]: number }>('/be/syncview'));

    const syncView = new Map<number, number>();
    Object.entries(syncViewObject).forEach(kv => syncView.set(parseInt(kv[0], 10), kv[1]));

    const syncRequest: TodoSyncRequest = {
      inserted: [],
      updated: [],
      removed: [],
      gets: []
    };

    const deleteLocal: number[] = [];

    await this.appDatabase.todos.toCollection().each(todo => {
      if (todo.id) {
        const serverTimestamp = syncView.get(todo.id);
        if (serverTimestamp) {
          if (todo.ts === -1) {
            syncRequest.removed.push(todo.id);
          } else if (todo.ts > serverTimestamp) {
            syncRequest.updated.push(todo);
          } else if (todo.ts < serverTimestamp) {
            syncRequest.gets.push(todo.id);
          }
          syncView.delete(todo.id);
        } else {
          // not on the server, either insert or delete locally
          if (todo.id < 0) {
            syncRequest.inserted.push(todo);
          } else {
            deleteLocal.push(todo.id);
          }
        }
      }
    });

    // all these ids are not in our local database, fetch them
    syncView.forEach((value, key) => syncRequest.gets.push(key));

    // delete local todos
    let deleted = false;
    for (const id of deleteLocal) {
      await this.appDatabase.todos.delete(id);
      deleted = true;
    }

    // if no changes end sync
    if (syncRequest.inserted.length === 0
      && syncRequest.updated.length === 0
      && syncRequest.removed.length === 0
      && syncRequest.gets.length === 0) {
      if (deleted) {
        this.updateSubject();
      }
      return Promise.resolve();
    }

    // send sync request to the server
    const syncResponse = await lastValueFrom(this.httpClient.post<TodoSyncResponse>('/be/sync',
      syncRequest));

    await this.appDatabase.transaction('rw', this.appDatabase.todos, async () => {
      if (syncResponse.gets && syncResponse.gets.length > 0) {
        await this.appDatabase.todos.bulkPut(syncResponse.gets);
      }
      if (syncResponse.inserted) {
        for (const kv of Object.entries(syncResponse.inserted)) {
          const oldId = parseInt(kv[0], 10);
          const todoFromDb = await this.appDatabase.todos.get(oldId);
          if (todoFromDb) {
            todoFromDb.id = kv[1].id;
            todoFromDb.ts = kv[1].ts;
            await this.appDatabase.todos.delete(oldId);
            await this.appDatabase.todos.add(todoFromDb);
          }
        }
      }
      if (syncResponse.updated) {
        Object.entries(syncResponse.updated).forEach(
          async (kv) => await this.appDatabase.todos.update(parseInt(kv[0], 10), {ts: kv[1]}));
      }
      if (syncResponse.removed) {
        syncResponse.removed.forEach(async (id) => await this.appDatabase.todos.delete(id));
      }
    });

    this.updateSubject();
    return Promise.resolve();
  }

  private updateSubject(): void {
    this.appDatabase.todos.where('ts').notEqual(-1).toArray().then(todos => {
      this.todosSubject.next(todos);
    });
  }

  private async getNextNewId(): Promise<number> {
    const first = await this.appDatabase.todos.toCollection().first();
    if (first && first.id) {
      if (first.id > 0) {
        return -1;
      } else {
        return first.id - 1;
      }
    }
    return -1;

  }


}
