import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {MessagesService} from '../../service/messages.service';
import {AlertController} from '@ionic/angular';
import {Todo} from '../../model/todo';
import {TodoService} from '../../service/todo.service';
import {NgForm} from '@angular/forms';

@Component({
  selector: 'app-edit',
  templateUrl: './edit.page.html',
  styleUrls: ['./edit.page.scss'],
})
export class EditPage implements OnInit {

  selectedTodo: Todo;

  constructor(private readonly route: ActivatedRoute,
              private readonly router: Router,
              private readonly messagesService: MessagesService,
              private readonly alertController: AlertController,
              private readonly todoService: TodoService) {
  }

  async ngOnInit() {
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

  async deleteTodo() {
    if (this.selectedTodo) {
      const alert = await this.alertController.create({
        header: 'Delete Todo',
        message: 'Do you really want to delete this entry?</strong>',
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

  async save(form: NgForm) {
    this.selectedTodo.subject = form.value.subject;
    this.selectedTodo.description = form.value.description;
    await this.todoService.save(this.selectedTodo);
    await this.messagesService.showSuccessToast('Todo successfully saved', 1000);
    await this.router.navigate(['/todos']);
  }

  private async reallyDeleteTodo() {
    await this.todoService.delete(this.selectedTodo);
    await this.router.navigate(['/todos']);
  }
}
