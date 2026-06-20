import { Component, inject, OnInit, signal } from '@angular/core';
import { AuthService } from '../service/auth.service';
import { MessagesService } from '../service/messages.service';
import { ActivatedRoute, RouterLink } from '@angular/router';
import {
  IonButton,
  IonCol,
  IonContent,
  IonGrid,
  IonHeader,
  IonRow,
  IonText,
  IonTitle,
  IonToolbar,
} from '@ionic/angular/standalone';

@Component({
  selector: 'app-signup-confirm',
  templateUrl: './signup-confirm.page.html',
  styleUrls: ['./signup-confirm.page.scss'],
  imports: [
    RouterLink,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonGrid,
    IonRow,
    IonCol,
    IonText,
    IonButton,
  ],
})
export class SignupConfirmPage implements OnInit {
  success = signal<boolean | null>(null);
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly messagesService = inject(MessagesService);

  async ngOnInit(): Promise<void> {
    const token = this.route.snapshot.paramMap.get('token');

    if (!token) {
      this.success.set(false);
      return;
    }

    const loading = await this.messagesService.showLoading('Processing Confirmation');

    this.authService.confirmSignup(token).subscribe(
      async (success) => {
        await loading.dismiss();
        this.success.set(success);
      },
      () => {
        loading.dismiss();
        this.success.set(false);
      },
    );
  }
}
