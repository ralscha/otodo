import {Component, HostListener} from '@angular/core';
import {NavController} from '@ionic/angular';
import {AuthService} from '../service/auth.service';
import {MessagesService} from '../service/messages.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.page.html',
  styleUrls: ['./login.page.scss'],
})
export class LoginPage {

  capslockOn = false;

  constructor(private readonly navCtrl: NavController,
              private readonly authService: AuthService,
              private readonly messagesService: MessagesService) {
  }

  async login(email: string, password: string) {
    const loading = await this.messagesService.showLoading('Logging in');

    this.authService.login(email, password)
      .subscribe(async (connectionState) => {

        await loading.dismiss();

        if (connectionState.isAdmin()) {
          this.navCtrl.navigateRoot('/users');
        } else if (connectionState.isUser()) {
          this.navCtrl.navigateRoot('/todos');
        } else {
          this.showLoginFailedToast();
        }
      });
  }

  @HostListener('window:keydown', ['$event'])
  onKeyDown(event) {
    this.capslockOn = event.getModifierState && event.getModifierState('CapsLock');
  }

  @HostListener('window:keyup', ['$event'])
  onKeyUp(event) {
    this.capslockOn = event.getModifierState && event.getModifierState('CapsLock');
  }

  private showLoginFailedToast() {
    this.messagesService.showErrorToast('Login failed');
  }

}
