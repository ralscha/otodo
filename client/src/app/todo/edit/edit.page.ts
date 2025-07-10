import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {MessagesService} from '../../service/messages.service';
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
  IonToolbar
} from '@ionic/angular/standalone';
import {Todo} from '../../model/todo';
import {TodoService} from '../../service/todo.service';
import {FormsModule, NgForm} from '@angular/forms';

@Component({
  selector: 'app-edit',
  templateUrl: './edit.page.html',
  styleUrls: ['./edit.page.scss'],
  imports: [FormsModule, IonHeader, IonToolbar, IonTitle, IonContent, IonGrid, IonRow, IonCol, IonItem, IonInput, IonText, IonButton, IonButtons, IonBackButton, IonTextarea, IonFab, IonFabButton, IonIcon]
})
export class EditPage implements OnInit {
  selectedTodo: Todo | undefined;
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly messagesService = inject(MessagesService);
  private readonly alertController = inject(AlertController);
  private readonly todoService = inject(TodoService);

  async ngOnInit(): Promise<void> {
    const todoIdString = this.route.snapshot.paramMap.get('id');
    if (todoIdString) {
      this.selectedTodo = await this.todoService.getTodo(parseInt(todoIdString, 10));
    } else {
      this.selectedTodo = {
        id: null,
        subject: null,
        description: null,
        ts: 0
      };
    }
  }

  async deleteTodo(): Promise<void> {
    if (this.selectedTodo) {
      const alert = await this.alertController.create({
        header: 'Delete Todo',
        message: 'Do you really want to delete this todo?',
        buttons: [
          {
            text: 'Cancel',
            role: 'cancel'
          }, {
            text: 'Delete Todo',
            handler: async () => this.reallyDeleteTodo()
          }
        ]
      });
      await alert.present();
    }
  }

  async save(form: NgForm): Promise<void> {
    if (this.selectedTodo) {
      this.selectedTodo.subject = form.value.subject;
      this.selectedTodo.description = form.value.description;
      await this.todoService.save(this.selectedTodo);
      await this.messagesService.showSuccessToast('Todo successfully saved', 1000);
      await this.router.navigate(['/todos']);
    }
  }

  private async reallyDeleteTodo(): Promise<void> {
    if (this.selectedTodo) {
      await this.todoService.delete(this.selectedTodo);
      await this.router.navigate(['/todos']);
    }
  }
}
