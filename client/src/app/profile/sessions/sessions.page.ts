import {Component, OnInit} from '@angular/core';
import {Session} from '../../model/session';
import {ProfileService} from '../../service/profile.service';
import * as UAParser from 'ua-parser-js';
import {MessagesService} from '../../service/messages.service';

@Component({
  selector: 'app-sessions',
  templateUrl: './sessions.page.html',
  styleUrls: ['./sessions.page.scss'],
})
export class SessionsPage implements OnInit {

  sessions: Session[] = [];

  constructor(private readonly profileService: ProfileService,
              private readonly messagesService: MessagesService) {
  }

  private static parseUA(userAgent: string): { uaBrowser: string, uaOs: string, uaDevice: string } {
    const ua = new UAParser(userAgent).getResult();
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

  ngOnInit() {
    this.profileService.fetchSessions().subscribe(response => {
      this.sessions = response;
      this.sessions.forEach(session => session.ua = SessionsPage.parseUA(session.userAgent));
    });
  }

  deleteSession(session: Session) {
    this.profileService.deleteSession(session.id).subscribe(() => {
      this.sessions = this.sessions.filter(s => s.id !== session.id);
    }, () => this.messagesService.showErrorToast());
  }

}
