import {Component} from '@angular/core';
import {NavController} from '@ionic/angular';
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
  selector: 'app-signup',
  templateUrl: './signup.page.html',
  styleUrls: ['./signup.page.scss'],
  imports: [FormsModule, IonHeader, IonToolbar, IonTitle, IonContent, IonGrid, IonRow, IonCol, IonItem, IonInput, IonText, IonButton, IonButtons, IonBackButton]
})
export class SignupPage {

  signUpSent = false;
  submitError: string | null = null;

  constructor(private readonly navCtrl: NavController,
              private readonly authService: AuthService,
              private readonly messagesService: MessagesService) {
  }

  async signup(email: string, password: string): Promise<void> {
    const loading = await this.messagesService.showLoading('Signing up');
    this.submitError = null;

    this.authService.signup(email, password)
      .subscribe(async (response) => {
        await loading.dismiss();
        if (!response) {
          this.signUpSent = true;
          await this.messagesService.showSuccessToast('Sign up successful');
        } else if (response === 'EMAIL_REGISTERED') {
          this.submitError = 'emailRegistered';
          await this.messagesService.showErrorToast('Email already registered');
        } else if (response === 'WEAK_PASSWORD') {
          this.submitError = 'weakPassword';
          await this.messagesService.showErrorToast('Weak password');
        }

      }, () => {
        loading.dismiss();
        this.messagesService.showErrorToast('Sign up failed');
      });

  }


}
