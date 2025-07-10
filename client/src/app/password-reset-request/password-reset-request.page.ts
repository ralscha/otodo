import {Component, inject} from '@angular/core';
import {AuthService} from '../service/auth.service';
import {MessagesService} from '../service/messages.service';
import {FormsModule} from '@angular/forms';
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
  IonToolbar
} from "@ionic/angular/standalone";

@Component({
  selector: 'app-password-reset-request',
  templateUrl: './password-reset-request.page.html',
  styleUrls: ['./password-reset-request.page.scss'],
  imports: [FormsModule, IonHeader, IonToolbar, IonTitle, IonContent, IonGrid, IonRow, IonCol, IonItem, IonInput, IonText, IonButton, IonButtons, IonBackButton]
})
export class PasswordResetRequestPage {
  resetSent = false;
  private readonly authService = inject(AuthService);
  private readonly messagesService = inject(MessagesService);

  async resetRequest(email: string): Promise<void> {

    const loading = await this.messagesService.showLoading('Sending email');

    this.authService.resetPasswordRequest(email)
      .subscribe(async () => {
        await loading.dismiss();
        await this.messagesService.showSuccessToast('Email successfully sent');
        this.resetSent = true;
      }, () => {
        loading.dismiss();
        this.messagesService.showErrorToast('Sending email failed');
      });

  }


}
