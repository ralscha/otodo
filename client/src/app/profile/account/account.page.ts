import { Component, inject, signal } from '@angular/core';
import { MessagesService } from '../../service/messages.service';
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
  NavController,
} from '@ionic/angular/standalone';
import { AppDatabase } from '../../model/app-database';
import { FormField, form, minLength, required, schema } from '@angular/forms/signals';
import { ProfileService } from '../../service/profile.service';

@Component({
  selector: 'app-account',
  templateUrl: './account.page.html',
  styleUrls: ['./account.page.scss'],
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
export class AccountPage {
  submitError = signal<string | null>(null);
  submitted = signal(false);
  readonly deleteModel = signal({ password: '' });
  readonly deleteForm = form(
    this.deleteModel,
    schema((path) => {
      required(path.password);
      minLength(path.password, 8);
    }),
  );
  private readonly navCtrl = inject(NavController);
  private readonly profileService = inject(ProfileService);
  private readonly messagesService = inject(MessagesService);
  private readonly appDatabase = inject(AppDatabase);
  private readonly alertController = inject(AlertController);

  async deleteAccount(): Promise<void> {
    this.submitted.set(true);
    this.deleteForm().markAsTouched();

    if (this.deleteForm().invalid()) {
      return;
    }

    this.submitError.set(null);
    const password = this.deleteModel().password;

    const alert = await this.alertController.create({
      header: 'Delete Account',
      message:
        'Do you really want to delete your account? This action is <strong>irreversible!</strong>',
      buttons: [
        {
          text: 'Cancel',
          role: 'cancel',
          handler: () => {
            this.resetDeleteForm();
          },
        },
        {
          text: 'Delete Account',
          handler: () => this.reallyDeleteAccount(password),
        },
      ],
    });

    await alert.present();
  }

  private async reallyDeleteAccount(password: string): Promise<void> {
    const loading = await this.messagesService.showLoading('Deleting account');

    this.profileService.deleteAccount(password).subscribe(
      async (success) => {
        await loading.dismiss();
        if (success) {
          await this.messagesService.showSuccessToast('Account successfully deleted');
          await this.appDatabase.authenticationToken.clear();
          await this.navCtrl.navigateRoot('/login');
        } else {
          this.resetDeleteForm();
          this.submitError.set('passwordInvalid');
          await this.messagesService.showErrorToast('Password invalid');
        }
      },
      () => {
        loading.dismiss();
        this.messagesService.showErrorToast('Deleting account failed');
      },
    );
  }

  private resetDeleteForm(): void {
    this.deleteForm().reset({ password: '' });
    this.submitted.set(false);
  }
}
