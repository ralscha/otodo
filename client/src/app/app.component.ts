import { Component, computed, inject } from '@angular/core';
import { AuthService } from './service/auth.service';
import { toSignal } from '@angular/core/rxjs-interop';
import { ConnectionService } from './service/connection.service';
import { RouterLink, RouterLinkActive } from '@angular/router';
import {
  IonApp,
  IonContent,
  IonHeader,
  IonIcon,
  IonItem,
  IonLabel,
  IonList,
  IonMenu,
  IonMenuToggle,
  IonRouterLink,
  IonRouterOutlet,
  IonSplitPane,
  IonTitle,
  IonToolbar,
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { checkmarkCircleOutline, logOut, people, person } from 'ionicons/icons';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  imports: [
    RouterLinkActive,
    RouterLink,
    IonRouterLink,
    IonSplitPane,
    IonMenu,
    IonApp,
    IonContent,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonList,
    IonMenuToggle,
    IonItem,
    IonIcon,
    IonLabel,
    IonRouterOutlet,
  ],
})
export class AppComponent {
  private readonly connectionService = inject(ConnectionService);
  private readonly connectionState = toSignal(this.connectionService.connectionState());

  authenticated = computed(() => this.connectionState()?.isAuthenticated() ?? false);
  appPages = computed(() => {
    const connectionState = this.connectionState();
    const pages: { title: string; url: string; icon: string }[] = [];

    if (connectionState?.isUser()) {
      pages.push({ title: 'Todos', url: '/todos', icon: 'checkmark-circle-outline' });
    }

    if (connectionState?.isAdmin()) {
      pages.push({ title: 'Users', url: '/users', icon: 'people' });
    }

    if (connectionState?.isOnlineAuthenticated()) {
      pages.push({ title: 'Profile', url: '/profile', icon: 'person' });
    }

    if (connectionState?.isAuthenticated()) {
      pages.push({ title: 'Log out', url: '/logout', icon: 'log-out' });
    }

    return pages;
  });

  constructor() {
    inject(AuthService);
    addIcons({ checkmarkCircleOutline, people, person, logOut });
  }
}
