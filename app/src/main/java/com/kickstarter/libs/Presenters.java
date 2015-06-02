package com.kickstarter.libs;

import android.content.Context;
import android.os.Bundle;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.UUID;

public class Presenters {
  private static final String PRESENTER_ID_KEY = "presenter_id";
  private static final String PRESENTER_STATE_KEY = "presenter_state";

  private static final Presenters instance = new Presenters();
  private final BiMap<String, Presenter> presenters = HashBiMap.create();

  public static Presenters getInstance() {
    return instance;
  }

  @SuppressWarnings("unchecked")
  public <T extends Presenter> T fetch(final Context context,
    final Class<T> presenterClass,
    final Bundle savedInstanceState) {
    final String id = fetchId(savedInstanceState);
    Presenter presenter = presenters.get(id);

    if (presenter == null) {
      try {
        presenter = presenterClass.newInstance();
      }
      catch (InstantiationException | IllegalAccessException e) {
        throw new RuntimeException(e);
      }
      presenters.put(id, presenter);
      presenter.onCreate(context, BundleUtils.maybeGetBundle(savedInstanceState, PRESENTER_STATE_KEY));
    }

    return (T) presenter;
  }

  public void destroy(final Presenter presenter) {
    presenter.onDestroy();
    presenters.inverse().remove(presenter);
  }

  public void save(final Presenter presenter, final Bundle envelope) {
    envelope.putString(PRESENTER_ID_KEY, presenters.inverse().get(presenter));

    final Bundle state = new Bundle();
    presenter.save(state);
    envelope.putBundle(PRESENTER_STATE_KEY, state);
  }

  protected String fetchId(final Bundle savedInstanceState) {
    return savedInstanceState != null ?
      savedInstanceState.getString(PRESENTER_ID_KEY) :
      UUID.randomUUID().toString();
  }
}