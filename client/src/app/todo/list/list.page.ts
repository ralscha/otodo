import {Component, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {Todo} from '../../model/todo';
import {TodoService} from '../../service/todo.service';
import {RouterLink} from '@angular/router';
import {AsyncPipe} from '@angular/common';
import {
  IonButtons,
  IonContent,
  IonFab,
  IonFabButton,
  IonHeader,
  IonIcon,
  IonItem,
  IonLabel,
  IonList,
  IonMenuButton,
  IonRefresher,
  IonRefresherContent,
  IonRouterLink,
  IonTitle,
  IonToolbar
} from "@ionic/angular/standalone";

@Component({
  selector: 'app-list',
  templateUrl: './list.page.html',
  styleUrls: ['./list.page.scss'],
  imports: [RouterLink, IonRouterLink, AsyncPipe, IonHeader, IonToolbar, IonTitle, IonContent, IonItem, IonButtons, IonMenuButton, IonRefresher, IonRefresherContent, IonList, IonLabel, IonFab, IonFabButton, IonIcon]
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
