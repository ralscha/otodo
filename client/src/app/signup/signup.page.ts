import { Component, inject, signal } from '@angular/core';
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
import { AuthService } from '../service/auth.service';
import { MessagesService } from '../service/messages.service';
import { email, FormField, form, minLength, required, schema } from '@angular/forms/signals';

@Component({
  selector: 'app-signup',
  templateUrl: './signup.page.html',
  styleUrls: ['./signup.page.scss'],
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
export class SignupPage {
  signUpSent = signal(false);
  submitError = signal<string | null>(null);
  submitted = signal(false);
  readonly signupModel = signal({ email: '', password: '' });
  readonly signupForm = form(
    this.signupModel,
    schema((path) => {
      required(path.email);
      email(path.email);
      required(path.password);
      minLength(path.password, 8);
    }),
  );
  private readonly authService = inject(AuthService);
  private readonly messagesService = inject(MessagesService);

  async signup(): Promise<void> {
    this.submitted.set(true);
    this.signupForm().markAsTouched();

    if (this.signupForm().invalid()) {
      return;
    }

    const loading = await this.messagesService.showLoading('Signing up');
    this.submitError.set(null);
    const signup = this.signupModel();

    this.authService.signup(signup.email, signup.password).subscribe(
      async (response) => {
        await loading.dismiss();
        if (!response) {
          this.signUpSent.set(true);
          await this.messagesService.showSuccessToast('Sign up successful');
        } else if (response === 'EMAIL_REGISTERED') {
          this.submitError.set('emailRegistered');
          await this.messagesService.showErrorToast('Email already registered');
        } else if (response === 'WEAK_PASSWORD') {
          this.submitError.set('weakPassword');
          await this.messagesService.showErrorToast('Weak password');
        }
      },
      () => {
        loading.dismiss();
        this.messagesService.showErrorToast('Sign up failed');
      },
    );
  }
}
