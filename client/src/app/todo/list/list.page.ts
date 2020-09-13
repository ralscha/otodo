import {Component, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {Todo} from '../../model/todo';
import {TodoService} from '../../service/todo.service';

@Component({
  selector: 'app-list',
  templateUrl: './list.page.html',
  styleUrls: ['./list.page.scss'],
})
export class ListPage implements OnInit {

  todos$!: Observable<Todo[]>;

  constructor(private readonly todoService: TodoService) {
  }

  ngOnInit(): void {
    this.todos$ = this.todoService.getTodos();
    this.todoService.requestSync();
  }

  refresh(event: Event): void {
    this.todoService.requestSync()
      .finally(() => (event as CustomEvent).detail.complete());
  }

}
