import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, RouterLink} from '@angular/router';
import {MessagesService} from '../../service/messages.service';
import {ProfileService} from '../../service/profile.service';
import {
  IonButton,
  IonCol,
  IonContent,
  IonGrid,
  IonHeader,
  IonRow,
  IonText,
  IonTitle,
  IonToolbar
} from "@ionic/angular/standalone";

@Component({
  selector: 'app-email-change-confirm',
  templateUrl: './email-change-confirm.page.html',
  styleUrls: ['./email-change-confirm.page.scss'],
  imports: [RouterLink, IonHeader, IonToolbar, IonTitle, IonContent, IonGrid, IonRow, IonCol, IonText, IonButton]
})
export class EmailChangeConfirmPage implements OnInit {
  success: boolean | null = null;
  private readonly profileService = inject(ProfileService);
  private readonly route = inject(ActivatedRoute);
  private readonly messagesService = inject(MessagesService);

  async ngOnInit(): Promise<void> {
    const token = this.route.snapshot.paramMap.get('token');

    if (!token) {
      this.success = false;
      return;
    }

    const loading = await this.messagesService.showLoading('Processing Confirmation');

    this.profileService.confirmEmailChange(token)
      .subscribe(async (success) => {
        await loading.dismiss();
        this.success = success;
      }, () => {
        loading.dismiss();
        this.success = false;
      });
  }

}
