import {Component, inject, viewChild} from '@angular/core';
import {MessagesService} from '../../service/messages.service';
import {FormsModule, NgForm} from '@angular/forms';
import {ProfileService} from '../../service/profile.service';
import {AuthService} from '../../service/auth.service';
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
  NavController
} from '@ionic/angular/standalone';

@Component({
  selector: 'app-password',
  templateUrl: './password.page.html',
  styleUrls: ['./password.page.scss'],
  imports: [FormsModule, IonHeader, IonToolbar, IonTitle, IonContent, IonGrid, IonRow, IonCol, IonItem, IonInput, IonText, IonButton, IonButtons, IonBackButton]
})
export class PasswordPage {
  submitError: string | null = null;
  readonly changeForm = viewChild.required<NgForm>('changeForm');
  private readonly profileService = inject(ProfileService);
  private readonly authService = inject(AuthService);
  private readonly navCtrl = inject(NavController);
  private readonly messagesService = inject(MessagesService);

  async changePassword(oldPassword: string, newPassword: string): Promise<void> {

    const loading = await this.messagesService.showLoading('Changing password');
    this.submitError = null;

    this.profileService.changePassword(oldPassword, newPassword)
      .subscribe(async (response) => {
        await loading.dismiss();
        if (!response) {
          this.changeForm().resetForm();
          await this.messagesService.showSuccessToast('Password successfully changed');
          await this.authService.deleteTokens();
          await this.navCtrl.navigateRoot('/login');
        } else if (response === 'INVALID') {
          this.submitError = 'passwordInvalid';
          await this.messagesService.showErrorToast('Old Password invalid');
        } else if (response === 'WEAK_PASSWORD') {
          this.submitError = 'weakPassword';
          await this.messagesService.showErrorToast('Weak password');
        }
      }, () => {
        loading.dismiss();
        this.messagesService.showErrorToast('Changing password failed');
      });

  }

}
