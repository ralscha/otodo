import { Component, inject, signal } from '@angular/core';
import { AuthService } from '../service/auth.service';
import { MessagesService } from '../service/messages.service';
import { email, FormField, form, required, schema } from '@angular/forms/signals';
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
  selector: 'app-password-reset-request',
  templateUrl: './password-reset-request.page.html',
  styleUrls: ['./password-reset-request.page.scss'],
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
export class PasswordResetRequestPage {
  resetSent = signal(false);
  submitted = signal(false);
  readonly resetRequestModel = signal({ email: '' });
  readonly resetRequestForm = form(
    this.resetRequestModel,
    schema((path) => {
      required(path.email);
      email(path.email);
    }),
  );
  private readonly authService = inject(AuthService);
  private readonly messagesService = inject(MessagesService);

  async resetRequest(): Promise<void> {
    this.submitted.set(true);
    this.resetRequestForm().markAsTouched();

    if (this.resetRequestForm().invalid()) {
      return;
    }

    const loading = await this.messagesService.showLoading('Sending email');

    this.authService.resetPasswordRequest(this.resetRequestModel().email).subscribe(
      async () => {
        await loading.dismiss();
        await this.messagesService.showSuccessToast('Email successfully sent');
        this.resetSent.set(true);
      },
      () => {
        loading.dismiss();
        this.messagesService.showErrorToast('Sending email failed');
      },
    );
  }
}
