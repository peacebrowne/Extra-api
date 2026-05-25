package com.example.extra.Enumerator;


public enum TaskStatus {

  /**
   * Newly created task, not yet accepted by any provider.
   */
  PENDING,

  /**
   * A provider has accepted the task and has been assigned.
   */
  ACCEPTED,

  /**
   * The task has been completed and confirmed (finalized).
   * Note: In some flows this constant represents the final approved/completed state.
   */
  COMPLETED,

  /**
   * Work on the task is currently in progress by the assigned provider.
   */
  IN_PROGRESS,

  /**
   * The task was cancelled before completion (by client or system).
   */
  CANCELLED,

  /**
   * Provider has submitted completion and is awaiting client confirmation.
   */
  PENDING_CONFIRMATION,

  /**
   * A dispute has been raised for this task and requires manual resolution.
   */
  DISPUTED;
}