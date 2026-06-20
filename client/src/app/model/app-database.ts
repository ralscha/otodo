import Dexie from 'dexie';
import { Service } from '@angular/core';
import { Todo } from './todo';
import { ClientError } from './client-error';

@Service()
export class AppDatabase extends Dexie {
  authenticationToken!: Dexie.Table<string, number>;
  invalidAuthenticationTokens!: Dexie.Table<string, number>;
  todos!: Dexie.Table<Todo, number>;
  errors!: Dexie.Table<ClientError, string>;

  constructor() {
    super('OTodoDatabase');
    this.version(1).stores({
      authenticationToken: '++',
      invalidAuthenticationTokens: '++',
      todos: 'id,ts',
      errors: '++id',
    });
  }
}
