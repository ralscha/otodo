import { Component, inject, signal } from '@angular/core';
import { ProfileService } from '../../service/profile.service';
import { MessagesService } from '../../service/messages.service';
import { email, FormField, form, minLength, required, schema } from '@angular/forms/signals';
import {
  IonBackButton,
  IonButton,
  IonButtons,
  IonCol,
  IonContent,
  IonGrid,
  IonHeader,
  IonInput,
  IonItem,
  IonRow,
  IonText,
  IonTitle,
  IonToolbar,
} from '@ionic/angular/standalone';

@Component({
  selector: 'app-email',
  templateUrl: './email.page.html',
  styleUrls: ['./email.page.scss'],
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
  ],
})
export class EmailPage {
  submitError = signal<string | null>(null);
  changeSent = signal(false);
  submitted = signal(false);
  readonly emailModel = signal({ password: '', newEmail: '' });
  readonly emailForm = form(
    this.emailModel,
    schema((path) => {
      required(path.password);
      minLength(path.password, 8);
      required(path.newEmail);
      email(path.newEmail);
    }),
  );
  private readonly profileService = inject(ProfileService);
  private readonly messagesService = inject(MessagesService);

  async changeEmail(): Promise<void> {
    this.submitted.set(true);
    this.emailForm().markAsTouched();

    if (this.emailForm().invalid()) {
      return;
    }

    const loading = await this.messagesService.showLoading('Saving email change request');
    this.submitError.set(null);
    const emailChange = this.emailModel();

    this.profileService.changeEmail(emailChange.newEmail, emailChange.password).subscribe(
      async (flag) => {
        await loading.dismiss();
        if (flag === 'SAME') {
          this.submitError.set('noChange');
          await this.messagesService.showErrorToast('New email same as old email');
        } else if (flag === 'USE') {
          this.submitError.set('emailRegistered');
          await this.messagesService.showErrorToast('Email already registered');
        } else if (flag === 'PASSWORD') {
          this.submitError.set('passwordInvalid');
          await this.messagesService.showErrorToast('Password invalid');
        } else {
          await this.messagesService.showSuccessToast('Email change request successfully saved');
          this.changeSent.set(true);
        }
      },
      () => {
        loading.dismiss();
        this.messagesService.showErrorToast();
      },
    );
  }
}
