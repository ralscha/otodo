import {Todo} from './todo';

export interface TodoSyncRequest {
  inserted: Todo[];
  updated: Todo[];
  removed: number[];
  gets: number[];
}
