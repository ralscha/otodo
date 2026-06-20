import { Component, inject, OnInit } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { TodoService } from '../../service/todo.service';
import { RouterLink } from '@angular/router';
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
  IonToolbar,
} from '@ionic/angular/standalone';

@Component({
  selector: 'app-list',
  templateUrl: './list.page.html',
  styleUrls: ['./list.page.scss'],
  imports: [
    RouterLink,
    IonRouterLink,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonItem,
    IonButtons,
    IonMenuButton,
    IonRefresher,
    IonRefresherContent,
    IonList,
    IonLabel,
    IonFab,
    IonFabButton,
    IonIcon,
  ],
})
export class ListPage implements OnInit {
  private readonly todoService = inject(TodoService);
  todos = toSignal(this.todoService.getTodos(), { initialValue: [] });

  ngOnInit(): void {
    this.todoService.requestSync();
  }

  refresh(event: Event): void {
    this.todoService.requestSync().finally(() => (event as CustomEvent).detail.complete());
  }
}
