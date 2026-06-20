import { Component, computed, inject } from '@angular/core';
import { httpResource } from '@angular/common/http';
import { Session } from '../../model/session';
import { ProfileService } from '../../service/profile.service';
import { UAParser } from 'ua-parser-js';
import { MessagesService } from '../../service/messages.service';
import { DatePipe } from '@angular/common';
import { RelativeTimePipe } from '../../pipe/relative-time.pipe';
import {
  IonBackButton,
  IonButton,
  IonButtons,
  IonCard,
  IonCardHeader,
  IonCardTitle,
  IonCol,
  IonContent,
  IonHeader,
  IonIcon,
  IonItem,
  IonLabel,
  IonRow,
  IonTitle,
  IonToolbar,
} from '@ionic/angular/standalone';

type SessionResponse = Omit<Session, 'ua'>;

@Component({
  selector: 'app-sessions',
  templateUrl: './sessions.page.html',
  styleUrls: ['./sessions.page.scss'],
  imports: [
    DatePipe,
    RelativeTimePipe,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonRow,
    IonCol,
    IonItem,
    IonButton,
    IonCard,
    IonCardHeader,
    IonCardTitle,
    IonLabel,
    IonIcon,
    IonBackButton,
    IonButtons,
  ],
})
export class SessionsPage {
  private readonly sessionsResource = httpResource<SessionResponse[]>(() => '/be/sessions', {
    defaultValue: [],
  });
  private readonly profileService = inject(ProfileService);
  private readonly messagesService = inject(MessagesService);

  sessions = computed(() =>
    this.sessionsResource.value().map((session) => ({
      ...session,
      ua: SessionsPage.parseUA(session.userAgent),
    })),
  );

  private static parseUA(userAgent: string): { uaBrowser: string; uaOs: string; uaDevice: string } {
    const ua = new UAParser(userAgent).getResult();
    return {
      uaBrowser: `${ua.browser.name} ${ua.browser.major}`,
      uaOs: `${ua.os.name} ${ua.os.version}`,
      uaDevice: ua.device.vendor
        ? `${ua.device.vendor}${ua.device.type ? `(${ua.device.type})` : ''}`
        : '',
    };
  }

  deleteSession(session: Session): void {
    this.profileService.deleteSession(session.id).subscribe(
      () => {
        this.sessionsResource.update((sessions) => sessions.filter((s) => s.id !== session.id));
      },
      () => this.messagesService.showErrorToast(),
    );
  }
}
