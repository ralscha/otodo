import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ConnectionService } from '../service/connection.service';
import { Router } from '@angular/router';
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
  selector: 'app-offline',
  templateUrl: './offline.page.html',
  styleUrls: ['./offline.page.scss'],
  imports: [
    IonContent,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonGrid,
    IonRow,
    IonCol,
    IonText,
    IonButton,
  ],
})
export class OfflinePage implements OnInit {
  private readonly connectionService = inject(ConnectionService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  reconnect(): void {
    this.connectionService.reconnect();
  }

  ngOnInit(): void {
    this.connectionService
      .connectionState()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((cs) => {
        if (cs.isOnline()) {
          if (cs.isUser()) {
            this.router.navigate(['/todos']);
          } else if (cs.isAdmin()) {
            this.router.navigate(['/users']);
          } else {
            this.router.navigate(['/login']);
          }
        }
      });
  }
}
