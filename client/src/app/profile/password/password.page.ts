import { Component, inject, signal } from '@angular/core';
import { MessagesService } from '../../service/messages.service';
import { FormField, form, minLength, required, schema } from '@angular/forms/signals';
import { ProfileService } from '../../service/profile.service';
import { AuthService } from '../../service/auth.service';
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
  NavController,
} from '@ionic/angular/standalone';

@Component({
  selector: 'app-password',
  templateUrl: './password.page.html',
  styleUrls: ['./password.page.scss'],
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
export class PasswordPage {
  submitError = signal<string | null>(null);
  submitted = signal(false);
  readonly passwordModel = signal({ oldPassword: '', newPassword: '' });
  readonly passwordForm = form(
    this.passwordModel,
    schema((path) => {
      required(path.oldPassword);
      minLength(path.oldPassword, 8);
      required(path.newPassword);
      minLength(path.newPassword, 8);
    }),
  );
  private readonly profileService = inject(ProfileService);
  private readonly authService = inject(AuthService);
  private readonly navCtrl = inject(NavController);
  private readonly messagesService = inject(MessagesService);

  async changePassword(): Promise<void> {
    this.submitted.set(true);
    this.passwordForm().markAsTouched();

    if (this.passwordForm().invalid()) {
      return;
    }

    const loading = await this.messagesService.showLoading('Changing password');
    this.submitError.set(null);
    const passwords = this.passwordModel();

    this.profileService.changePassword(passwords.oldPassword, passwords.newPassword).subscribe(
      async (response) => {
        await loading.dismiss();
        if (!response) {
          this.passwordForm().reset({ oldPassword: '', newPassword: '' });
          this.submitted.set(false);
          await this.messagesService.showSuccessToast('Password successfully changed');
          await this.authService.deleteTokens();
          await this.navCtrl.navigateRoot('/login');
        } else if (response === 'INVALID') {
          this.submitError.set('passwordInvalid');
          await this.messagesService.showErrorToast('Old Password invalid');
        } else if (response === 'WEAK_PASSWORD') {
          this.submitError.set('weakPassword');
          await this.messagesService.showErrorToast('Weak password');
        }
      },
      () => {
        loading.dismiss();
        this.messagesService.showErrorToast('Changing password failed');
      },
    );
  }
}
