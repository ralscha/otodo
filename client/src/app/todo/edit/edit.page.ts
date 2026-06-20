import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessagesService } from '../../service/messages.service';
import {
  AlertController,
  IonBackButton,
  IonButton,
  IonButtons,
  IonCol,
  IonContent,
  IonFab,
  IonFabButton,
  IonGrid,
  IonHeader,
  IonIcon,
  IonInput,
  IonItem,
  IonRow,
  IonText,
  IonTextarea,
  IonTitle,
  IonToolbar,
} from '@ionic/angular/standalone';
import { Todo } from '../../model/todo';
import { TodoService } from '../../service/todo.service';
import { FormField, form, required, schema } from '@angular/forms/signals';

@Component({
  selector: 'app-edit',
  templateUrl: './edit.page.html',
  styleUrls: ['./edit.page.scss'],
  imports: [
    FormField,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonGrid,
    IonRow,
    IonCol,
    IonItem,
    IonInput,
    IonText,
    IonButton,
    IonButtons,
    IonBackButton,
    IonTextarea,
    IonFab,
    IonFabButton,
    IonIcon,
  ],
})
export class EditPage implements OnInit {
  selectedTodo = signal<Todo | undefined>(undefined);
  submitted = signal(false);
  readonly todoModel = signal({ subject: '', description: '' });
  readonly todoForm = form(
    this.todoModel,
    schema((path) => {
      required(path.subject);
    }),
  );
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly messagesService = inject(MessagesService);
  private readonly alertController = inject(AlertController);
  private readonly todoService = inject(TodoService);

  async ngOnInit(): Promise<void> {
    const todoIdString = this.route.snapshot.paramMap.get('id');
    if (todoIdString) {
      const todo = await this.todoService.getTodo(parseInt(todoIdString, 10));
      this.selectedTodo.set(todo);
      this.todoForm().reset({
        subject: todo?.subject ?? '',
        description: todo?.description ?? '',
      });
    } else {
      this.selectedTodo.set({
        id: null,
        subject: null,
        description: null,
        ts: 0,
      });
      this.todoForm().reset({ subject: '', description: '' });
    }
  }

  async deleteTodo(): Promise<void> {
    if (this.selectedTodo()) {
      const alert = await this.alertController.create({
        header: 'Delete Todo',
        message: 'Do you really want to delete this todo?',
        buttons: [
          {
            text: 'Cancel',
            role: 'cancel',
          },
          {
            text: 'Delete Todo',
            handler: async () => this.reallyDeleteTodo(),
          },
        ],
      });
      await alert.present();
    }
  }

  async save(): Promise<void> {
    this.submitted.set(true);
    this.todoForm().markAsTouched();

    const selectedTodo = this.selectedTodo();
    if (selectedTodo && !this.todoForm().invalid()) {
      const formValue = this.todoModel();
      selectedTodo.subject = formValue.subject;
      selectedTodo.description = formValue.description;
      await this.todoService.save(selectedTodo);
      await this.messagesService.showSuccessToast('Todo successfully saved', 1000);
      await this.router.navigate(['/todos']);
    }
  }

  private async reallyDeleteTodo(): Promise<void> {
    const selectedTodo = this.selectedTodo();
    if (selectedTodo) {
      await this.todoService.delete(selectedTodo);
      await this.router.navigate(['/todos']);
    }
  }
}
