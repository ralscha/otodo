import {Component, OnInit} from '@angular/core';
import {AuthService} from '../service/auth.service';
import {MessagesService} from '../service/messages.service';
import {ActivatedRoute, RouterLink} from '@angular/router';
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
  selector: 'app-signup-confirm',
  templateUrl: './signup-confirm.page.html',
  styleUrls: ['./signup-confirm.page.scss'],
  imports: [RouterLink, IonHeader, IonToolbar, IonTitle, IonContent, IonGrid, IonRow, IonCol, IonText, IonButton]
})
export class SignupConfirmPage implements OnInit {

  success: boolean | null = null;

  constructor(private readonly authService: AuthService,
              private readonly route: ActivatedRoute,
              private readonly messagesService: MessagesService) {
  }

  async ngOnInit(): Promise<void> {
    const token = this.route.snapshot.paramMap.get('token');

    if (!token) {
      this.success = false;
      return;
    }

    const loading = await this.messagesService.showLoading('Processing Confirmation');

    this.authService.confirmSignup(token)
      .subscribe(async (success) => {
        await loading.dismiss();
        this.success = success;
      }, () => {
        loading.dismiss();
        this.success = false;
      });
  }

}
