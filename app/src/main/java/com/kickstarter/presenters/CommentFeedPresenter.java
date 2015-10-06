package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.RxUtils;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.apiresponses.CommentsEnvelope;
import com.kickstarter.ui.activities.CommentFeedActivity;
import com.kickstarter.ui.adapters.CommentFeedAdapter;
import com.kickstarter.ui.viewholders.CommentViewHolder;
import com.kickstarter.ui.viewholders.ProjectContextViewHolder;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class CommentFeedPresenter extends Presenter<CommentFeedActivity> implements CommentFeedAdapter.Delegate {
  private final PublishSubject<Void> contextClick = PublishSubject.create();

  @Inject ApiClient client;
  @Inject CurrentUser currentUser;

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);
  }

  // todo: add pagination to comments
  public void takeProject(@NonNull final Project project) {
    final Observable<List<Comment>> comments = client.fetchProjectComments(project)
      .map(CommentsEnvelope::comments)
      .takeUntil(List::isEmpty);

    // we want this to be a combineLatestPair
    final Observable<Pair<CommentFeedActivity, List<Comment>>> viewAndComments =
      RxUtils.takePairWhen(viewSubject, comments);

    addSubscription(viewAndComments
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vc -> vc.first.loadProjectComments(project, vc.second)));

    addSubscription(RxUtils.takeWhen(viewSubject, contextClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(CommentFeedActivity::onBackPressed)
    );
  }

  public void projectContextClicked() {
    contextClick.onNext(null);
  }
}