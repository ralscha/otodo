import {Component, inject, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {User} from '../model/user';
import {HttpClient} from '@angular/common/http';
import {filter, map, mergeMap, shareReplay, tap, toArray} from 'rxjs/operators';
import {AsyncPipe, DatePipe} from '@angular/common';
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
  IonToolbar
} from "@ionic/angular/standalone";

@Component({
  selector: 'app-users',
  templateUrl: './users.page.html',
  styleUrls: ['./users.page.scss'],
  imports: [AsyncPipe, DatePipe, IonHeader, IonToolbar, IonTitle, IonContent, IonItem, IonRefresher, IonRefresherContent, IonSearchbar, IonSelect, IonSelectOption, IonList, IonItemSliding, IonLabel, IonNote, IonItemOptions, IonItemOption, IonMenuButton, IonButtons]
})
export class UsersPage implements OnInit {
  users$!: Observable<User[]>;
  private readonly httpClient = inject(HttpClient);
  private allUsers$!: Observable<User[]>;
  private httpGetUsers: Observable<User[]> = this.httpClient.get<User[]>('/be/admin/users');
  private searchFilter: string | null = null;
  private selectFilter: string | null = null;

  ngOnInit(): void {
    this.allUsers$ = this.httpGetUsers.pipe(shareReplay());
    this.users$ = this.allUsers$;
  }

  refresh(event: Event): void {
    this.allUsers$ = this.httpGetUsers.pipe(tap(() => (event as CustomEvent).detail.complete()), shareReplay());
    this.doFilter();
  }

  activate(user: User, slidingItem: IonItemSliding): void {
    slidingItem.close();
    this.httpClient.post<void>('/be/admin/activate', user.id)
      .subscribe(() => user.expired = false);
  }

  disable(user: User, slidingItem: IonItemSliding): void {
    slidingItem.close();
    this.httpClient.post<void>('/be/admin/disable', user.id)
      .subscribe(() => user.enabled = false);
  }

  enable(user: User, slidingItem: IonItemSliding): void {
    slidingItem.close();
    this.httpClient.post<void>('/be/admin/enable', user.id)
      .subscribe(() => user.enabled = true);
  }

  delete(user: User, slidingItem: IonItemSliding): void {
    slidingItem.close();
    this.httpClient.post<void>('/be/admin/delete', user.id)
      .subscribe(() => {
        this.users$ = this.allUsers$.pipe(map(users => users.filter(u => u.id !== user.id)));
      });
  }

  onSearch(event: Event): void {
    // @ts-ignore
    this.searchFilter = event.target.value;
    this.doFilter();
  }

  onFilterChange(event: Event): void {
    // @ts-ignore
    this.selectFilter = event.target.value;
    this.doFilter();
  }

  private doFilter(): void {
    const filterFns: ((user: User) => boolean)[] = [];

    if (this.selectFilter) {
      switch (this.selectFilter) {
        case 'disabled':
          filterFns.push(user => !user.enabled);
          break;
        case 'enabled':
          filterFns.push(user => user.enabled);
          break;
        case 'inactive':
          filterFns.push(user => user.expired);
          break;
        case 'admin':
          filterFns.push(user => user.admin);
          break;
        default:
          this.users$ = this.allUsers$;
      }
    }

    if (this.searchFilter !== null) {
      filterFns.push(user => this.searchFilter !== null ? user.email.includes(this.searchFilter) : true);
    }

    this.filter(user => filterFns.every(fn => fn(user)));
  }

  private filter(filterFn: (user: User) => boolean): void {
    this.users$ = this.allUsers$.pipe(
      mergeMap(users => users),
      filter(filterFn),
      toArray());
  }
}
