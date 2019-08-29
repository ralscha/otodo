import {Todo} from './todo';

export interface TodoSyncResponse {
  gets: Todo[];
  updated: { [key: string]: number };
  inserted: { [key: string]: { id: number, ts: number } };
  removed: number[];
}
