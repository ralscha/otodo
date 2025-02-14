import {Component, OnInit} from '@angular/core';
import {Session} from '../../model/session';
import {ProfileService} from '../../service/profile.service';
import {UAParser} from 'ua-parser-js'
import {MessagesService} from '../../service/messages.service';
import {DatePipe} from '@angular/common';
import {RelativeTimePipe} from '../../pipe/relative-time.pipe';
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
  IonToolbar
} from "@ionic/angular/standalone";

@Component({
  selector: 'app-sessions',
  templateUrl: './sessions.page.html',
  styleUrls: ['./sessions.page.scss'],
  imports: [DatePipe, RelativeTimePipe, IonHeader, IonToolbar, IonTitle, IonContent, IonRow, IonCol, IonItem, IonButton, IonCard, IonCardHeader, IonCardTitle, IonLabel, IonIcon, IonBackButton, IonButtons]
})
export class SessionsPage implements OnInit {

  sessions: Session[] = [];

  constructor(private readonly profileService: ProfileService,
              private readonly messagesService: MessagesService) {
  }

  private static parseUA(userAgent: string): { uaBrowser: string, uaOs: string, uaDevice: string } {
    const ua = new UAParser(userAgent).getResult();
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const result: any = {};
    result.uaBrowser = `${ua.browser.name} ${ua.browser.major}`;
    result.uaOs = `${ua.os.name} ${ua.os.version}`;
    if (ua.device.vendor) {
      result.uaDevice = `${ua.device.vendor}${ua.device.type ? `(${ua.device.type})` : ''}`;
    } else {
      result.uaDevice = '';
    }
    return result;
  }

  ngOnInit(): void {
    this.profileService.fetchSessions().subscribe(response => {
      this.sessions = response;
      this.sessions.forEach(session => session.ua = SessionsPage.parseUA(session.userAgent));
    });
  }

  deleteSession(session: Session): void {
    this.profileService.deleteSession(session.id).subscribe(() => {
      this.sessions = this.sessions.filter(s => s.id !== session.id);
    }, () => this.messagesService.showErrorToast());
  }

}
