import { Component, computed, inject, signal } from '@angular/core';
import { User } from '../model/user';
import { HttpClient, httpResource } from '@angular/common/http';
import { DatePipe } from '@angular/common';
import {
  IonButtons,
  IonContent,
  IonHeader,
  IonItem,
  IonItemOption,
  IonItemOptions,
  IonItemSliding,
  IonLabel,
  IonList,
  IonMenuButton,
  IonNote,
  IonRefresher,
  IonRefresherContent,
  IonSearchbar,
  IonSelect,
  IonSelectOption,
  IonTitle,
  IonToolbar,
} from '@ionic/angular/standalone';

@Component({
  selector: 'app-users',
  templateUrl: './users.page.html',
  styleUrls: ['./users.page.scss'],
  imports: [
    DatePipe,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonItem,
    IonRefresher,
    IonRefresherContent,
    IonSearchbar,
    IonSelect,
    IonSelectOption,
    IonList,
    IonItemSliding,
    IonLabel,
    IonNote,
    IonItemOptions,
    IonItemOption,
    IonMenuButton,
    IonButtons,
  ],
})
export class UsersPage {
  private readonly usersResource = httpResource<User[]>(() => '/be/admin/users', {
    defaultValue: [],
  });
  private readonly httpClient = inject(HttpClient);
  private readonly searchFilter = signal<string | null>(null);
  private readonly selectFilter = signal<string | null>(null);

  users = computed(() => {
    const searchFilter = this.searchFilter();
    const selectFilter = this.selectFilter();

    return this.usersResource.value().filter((user) => {
      if (searchFilter && !user.email.includes(searchFilter)) {
        return false;
      }

      switch (selectFilter) {
        case 'disabled':
          return !user.enabled;
        case 'enabled':
          return user.enabled;
        case 'inactive':
          return user.expired;
        case 'admin':
          return user.admin;
        default:
          return true;
      }
    });
  });

  refresh(event: Event): void {
    this.usersResource.reload();
    (event as CustomEvent).detail.complete();
  }

  activate(user: User, slidingItem: IonItemSliding): void {
    slidingItem.close();
    this.httpClient.post<void>('/be/admin/activate', user.id).subscribe(() => {
      this.updateUser(user.id, { expired: false });
    });
  }

  disable(user: User, slidingItem: IonItemSliding): void {
    slidingItem.close();
    this.httpClient.post<void>('/be/admin/disable', user.id).subscribe(() => {
      this.updateUser(user.id, { enabled: false });
    });
  }

  enable(user: User, slidingItem: IonItemSliding): void {
    slidingItem.close();
    this.httpClient.post<void>('/be/admin/enable', user.id).subscribe(() => {
      this.updateUser(user.id, { enabled: true });
    });
  }

  delete(user: User, slidingItem: IonItemSliding): void {
    slidingItem.close();
    this.httpClient.post<void>('/be/admin/delete', user.id).subscribe(() => {
      this.usersResource.update((users) => users.filter((u) => u.id !== user.id));
    });
  }

  onSearch(event: CustomEvent<{ value?: string | null }>): void {
    this.searchFilter.set(event.detail.value ?? null);
  }

  onFilterChange(event: CustomEvent<{ value?: string | null }>): void {
    this.selectFilter.set(event.detail.value ?? null);
  }

  private updateUser(userId: string, changes: Partial<User>): void {
    this.usersResource.update((users) =>
      users.map((user) => (user.id === userId ? { ...user, ...changes } : user)),
    );
  }
}
