import { Component, inject, OnInit, signal } from '@angular/core';
import { AuthService } from '../service/auth.service';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MessagesService } from '../service/messages.service';
import { FormField, form, minLength, required, schema } from '@angular/forms/signals';
import {
  IonButton,
  IonCol,
  IonContent,
  IonGrid,
  IonHeader,
  IonInput,
  IonItem,
  IonRouterLink,
  IonRow,
  IonText,
  IonTitle,
  IonToolbar,
} from '@ionic/angular/standalone';

@Component({
  selector: 'app-password-reset',
  templateUrl: './password-reset.page.html',
  styleUrls: ['./password-reset.page.scss'],
  imports: [
    FormField,
    RouterLink,
    IonRouterLink,
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
  ],
})
export class PasswordResetPage implements OnInit {
  success = signal<boolean | null>(null);
  submitError = signal<string | null>(null);
  submitted = signal(false);
  readonly resetModel = signal({ password: '' });
  readonly resetForm = form(
    this.resetModel,
    schema((path) => {
      required(path.password);
      minLength(path.password, 8);
    }),
  );
  private resetToken: string | null = null;
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly messagesService = inject(MessagesService);

  async ngOnInit(): Promise<void> {
    this.resetToken = this.route.snapshot.paramMap.get('token');

    if (!this.resetToken) {
      this.success.set(false);
    }
  }

  async reset(): Promise<void> {
    this.submitted.set(true);
    this.resetForm().markAsTouched();

    if (this.resetForm().invalid() || this.resetToken === null) {
      return;
    }

    const loading = await this.messagesService.showLoading('Changing Password');
    this.authService.resetPassword(this.resetToken, this.resetModel().password).subscribe(
      async (response) => {
        await loading.dismiss();
        this.submitError.set(null);
        if (!response) {
          await this.messagesService.showSuccessToast('Password successfully changed');
          this.success.set(true);
        } else if (response === 'WEAK_PASSWORD') {
          this.submitError.set('weakPassword');
          await this.messagesService.showErrorToast('Weak password');
        } else if (response === 'INVALID') {
          this.success.set(false);
          await this.messagesService.showErrorToast('Password change failed');
        }
      },
      () => {
        this.success.set(false);
        loading.dismiss();
        this.messagesService.showErrorToast('Password change failed');
      },
    );
  }
}
