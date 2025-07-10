import {Component, inject, ViewChild} from '@angular/core';
import {MessagesService} from '../../service/messages.service';
import {
  AlertController,
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
import {AppDatabase} from '../../model/app-database';
import {FormsModule, NgForm} from '@angular/forms';
import {ProfileService} from '../../service/profile.service';

@Component({
  selector: 'app-account',
  templateUrl: './account.page.html',
  styleUrls: ['./account.page.scss'],
  imports: [FormsModule, IonHeader, IonToolbar, IonTitle, IonContent, IonGrid, IonRow, IonCol, IonItem, IonInput, IonText, IonButton, IonButtons, IonBackButton]
})
export class AccountPage {
  submitError: string | null = null;
  @ViewChild('deleteForm')
  deleteForm!: NgForm;
  private readonly navCtrl = inject(NavController);
  private readonly profileService = inject(ProfileService);
  private readonly messagesService = inject(MessagesService);
  private readonly appDatabase = inject(AppDatabase);
  private readonly alertController = inject(AlertController);

  async deleteAccount(password: string): Promise<void> {
    this.submitError = null;

    const alert = await this.alertController.create({
      header: 'Delete Account',
      message: 'Do you really want to delete your account? This action is <strong>irreversible!</strong>',
      buttons: [
        {
          text: 'Cancel',
          role: 'cancel',
          handler: () => {
            this.deleteForm.resetForm();
          }
        }, {
          text: 'Delete Account',
          handler: () => this.reallyDeleteAccount(password)
        }
      ]
    });

    await alert.present();

  }

  private async reallyDeleteAccount(password: string): Promise<void> {
    const loading = await this.messagesService.showLoading('Deleting account');

    this.profileService.deleteAccount(password)
      .subscribe(async (success) => {
        await loading.dismiss();
        if (success) {
          await this.messagesService.showSuccessToast('Account successfully deleted');
          await this.appDatabase.authenticationToken.clear();
          await this.navCtrl.navigateRoot('/login');
        } else {
          this.deleteForm.resetForm();
          this.submitError = 'passwordInvalid';
          await this.messagesService.showErrorToast('Password invalid');
        }
      }, () => {
        loading.dismiss();
        this.messagesService.showErrorToast('Deleting account failed');
      });
  }

}
