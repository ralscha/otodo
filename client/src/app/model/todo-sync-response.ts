import {Todo} from './todo';

export interface TodoSyncResponse {
  gets: Todo[];
  updated: Record<string, number>;
  inserted: Record<string, { id: number, ts: number }>;
  removed: number[];
}
