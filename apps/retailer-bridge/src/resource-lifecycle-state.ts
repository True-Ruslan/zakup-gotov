import {
  canonicalObservedResourceUrl,
  fulfillmentContextResource,
} from "./resource-observation-policy";

export type ResourceLifecycleState = Readonly<{
  observedResourceUrls: ReadonlySet<string>;
  currentFulfillmentContextKey: string | null;
  contextSignalFloorStartTime: number;
  awaitingFreshContext: boolean;
}>;

export const INITIAL_RESOURCE_LIFECYCLE_STATE: ResourceLifecycleState = Object.freeze({
  observedResourceUrls: Object.freeze(new Set<string>()),
  currentFulfillmentContextKey: null,
  contextSignalFloorStartTime: Number.NEGATIVE_INFINITY,
  awaitingFreshContext: false,
});

export type RememberResourceResult = Readonly<{
  state: ResourceLifecycleState;
  changed: boolean;
  contextChanged: boolean;
}>;

const NO_CHANGE = (state: ResourceLifecycleState): RememberResourceResult => ({
  state,
  changed: false,
  contextChanged: false,
});

export function rememberAllowedResource(
  state: ResourceLifecycleState,
  rawUrl: string,
  pageUrl: URL,
  startTime: number,
): RememberResourceResult {
  const canonical = canonicalObservedResourceUrl(rawUrl, pageUrl);
  if (!canonical) return NO_CHANGE(state);

  const context = fulfillmentContextResource(rawUrl, pageUrl);
  if (state.currentFulfillmentContextKey && !context) {
    return NO_CHANGE(state);
  }

  if (context && startTime < state.contextSignalFloorStartTime) {
    return NO_CHANGE(state);
  }

  if (context && state.awaitingFreshContext) {
    return {
      state: {
        observedResourceUrls: new Set([context.canonicalUrl]),
        currentFulfillmentContextKey: context.contextKey,
        contextSignalFloorStartTime: startTime,
        awaitingFreshContext: false,
      },
      changed: true,
      contextChanged: false,
    };
  }

  if (
    context &&
    state.currentFulfillmentContextKey &&
    context.contextKey !== state.currentFulfillmentContextKey
  ) {
    return {
      state: {
        observedResourceUrls: new Set([context.canonicalUrl]),
        currentFulfillmentContextKey: context.contextKey,
        contextSignalFloorStartTime: startTime,
        awaitingFreshContext: false,
      },
      changed: true,
      contextChanged: true,
    };
  }

  if (state.observedResourceUrls.has(canonical)) {
    return NO_CHANGE(state);
  }

  const nextUrls = new Set(state.observedResourceUrls);
  nextUrls.add(canonical);
  return {
    state: { ...state, observedResourceUrls: nextUrls },
    changed: true,
    contextChanged: false,
  };
}

/**
 * A same-document (SPA) navigation always re-arms a fresh-context wait: a navigation
 * may represent a genuine store/session change (already-hardened invariant for
 * Perekrestok/Pyaterochka, #54/#153), and this module has no reliable way to tell
 * "just a new route, same store" apart from "a new store, no context resource yet"
 * from resource evidence alone. Discarding the prior context and waiting for an
 * explicit re-confirmation is the fail-closed choice.
 */
export function applySameDocumentNavigationReset(
  signalFloorStartTime: number,
): ResourceLifecycleState {
  return {
    observedResourceUrls: new Set(),
    currentFulfillmentContextKey: null,
    contextSignalFloorStartTime: signalFloorStartTime,
    awaitingFreshContext: true,
  };
}

export function retainCurrentFulfillmentResource(
  state: ResourceLifecycleState,
  pageUrl: URL,
): ResourceLifecycleState {
  const currentContext = state.currentFulfillmentContextKey;
  if (!currentContext) {
    return { ...state, observedResourceUrls: new Set() };
  }

  const retained = new Set(
    [...state.observedResourceUrls].filter(
      (url) => fulfillmentContextResource(url, pageUrl)?.contextKey === currentContext,
    ),
  );
  return { ...state, observedResourceUrls: retained };
}
