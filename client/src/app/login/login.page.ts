import {Component, HostListener} from '@angular/core';
import {NavController} from '@ionic/angular';
import {AuthService} from '../service/auth.service';
import {MessagesService} from '../service/messages.service';
import {FormsModule} from '@angular/forms';
import {RouterLink} from '@angular/router';
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
  IonToolbar
} from "@ionic/angular/standalone";

@Component({
  selector: 'app-login',
  templateUrl: './login.page.html',
  styleUrls: ['./login.page.scss'],
  imports: [FormsModule, RouterLink, IonRouterLink, IonHeader, IonToolbar, IonTitle, IonContent, IonGrid, IonRow, IonCol, IonItem, IonInput, IonText, IonButton]
})
export class LoginPage {

  capslockOn = false;

  constructor(private readonly navCtrl: NavController,
              private readonly authService: AuthService,
              private readonly messagesService: MessagesService) {
  }

  async login(email: string, password: string): Promise<void> {
    const loading = await this.messagesService.showLoading('Logging in');

    this.authService.login(email, password)
      .subscribe(async (connectionState) => {

        await loading.dismiss();

        if (connectionState.isAdmin()) {
          await this.navCtrl.navigateRoot('/users');
        } else if (connectionState.isUser()) {
          await this.navCtrl.navigateRoot('/todos');
        } else {
          this.showLoginFailedToast();
        }
      });
  }

  @HostListener('window:keydown', ['$event'])
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  onKeyDown(event: any): void {
    this.capslockOn = event.getModifierState && event.getModifierState('CapsLock');
  }

  @HostListener('window:keyup', ['$event'])
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  onKeyUp(event: any): void {
    this.capslockOn = event.getModifierState && event.getModifierState('CapsLock');
  }

  private showLoginFailedToast(): void {
    this.messagesService.showErrorToast('Login failed');
  }

}
