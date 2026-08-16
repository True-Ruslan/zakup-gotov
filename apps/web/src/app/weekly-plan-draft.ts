import type { components } from "@zakup-gotov/api-client";

type QuantityUnit = components["schemas"]["QuantityInputUnit"];
type WeeklyPlanDay = components["schemas"]["WeeklyPlanDay"];

export const WEEKLY_PLAN_DRAFT_STORAGE_KEY = "zakup-gotov.weekly-plan-draft.v1";

const DAYS = new Set<WeeklyPlanDay>([
  "MONDAY",
  "TUESDAY",
  "WEDNESDAY",
  "THURSDAY",
  "FRIDAY",
  "SATURDAY",
  "SUNDAY",
]);

const UNITS = new Set<QuantityUnit>([
  "PIECE",
  "GRAM",
  "KILOGRAM",
  "MILLILITER",
  "LITER",
]);

export type WeeklyPlanDraftIngredient = {
  requirement: string;
  amount: string;
  unit: QuantityUnit;
};

export type WeeklyPlanDraftOccurrence = {
  day: WeeklyPlanDay;
  targetServings: string;
  title: string;
  baseServings: string;
  ingredients: WeeklyPlanDraftIngredient[];
};

export type WeeklyPlanDraftPantryItem = {
  requirement: string;
  amount: string;
  unit: QuantityUnit;
};

export type WeeklyPlanDraftV1 = {
  version: 1;
  locality: string;
  occurrences: WeeklyPlanDraftOccurrence[];
  pantry: WeeklyPlanDraftPantryItem[];
};

export type DraftReadResult =
  | { kind: "ready"; draft: WeeklyPlanDraftV1 | null }
  | { kind: "unavailable" };

export type DraftWriteResult = { kind: "saved" } | { kind: "unavailable" };
export type DraftRemoveResult = { kind: "removed" } | { kind: "unavailable" };

type JsonObject = Record<string, unknown>;

function isObject(value: unknown): value is JsonObject {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

function boundedString(value: unknown, maxLength?: number): string | null {
  if (typeof value !== "string") return null;
  if (maxLength !== undefined && value.length > maxLength) return null;
  return value;
}

function quantityUnit(value: unknown): QuantityUnit | null {
  return typeof value === "string" && UNITS.has(value as QuantityUnit)
    ? value as QuantityUnit
    : null;
}

function weeklyPlanDay(value: unknown): WeeklyPlanDay | null {
  return typeof value === "string" && DAYS.has(value as WeeklyPlanDay)
    ? value as WeeklyPlanDay
    : null;
}

function ingredient(value: unknown): WeeklyPlanDraftIngredient | null {
  if (!isObject(value)) return null;
  const requirement = boundedString(value.requirement, 240);
  const amount = boundedString(value.amount);
  const unit = quantityUnit(value.unit);
  if (requirement === null || amount === null || unit === null) return null;
  return { requirement, amount, unit };
}

function occurrence(value: unknown): WeeklyPlanDraftOccurrence | null {
  if (!isObject(value)) return null;
  const day = weeklyPlanDay(value.day);
  const targetServings = boundedString(value.targetServings);
  const title = boundedString(value.title, 240);
  const baseServings = boundedString(value.baseServings);
  if (
    day === null
    || targetServings === null
    || title === null
    || baseServings === null
    || !Array.isArray(value.ingredients)
    || value.ingredients.length < 1
    || value.ingredients.length > 100
  ) {
    return null;
  }

  const ingredients: WeeklyPlanDraftIngredient[] = [];
  for (const candidate of value.ingredients) {
    const decoded = ingredient(candidate);
    if (decoded === null) return null;
    ingredients.push(decoded);
  }
  return { day, targetServings, title, baseServings, ingredients };
}

function pantryItem(value: unknown): WeeklyPlanDraftPantryItem | null {
  if (!isObject(value)) return null;
  const requirement = boundedString(value.requirement, 240);
  const amount = boundedString(value.amount);
  const unit = quantityUnit(value.unit);
  if (requirement === null || amount === null || unit === null) return null;
  return { requirement, amount, unit };
}

function decodeValue(value: unknown): WeeklyPlanDraftV1 | null {
  if (
    !isObject(value)
    || value.version !== 1
    || !Array.isArray(value.occurrences)
    || value.occurrences.length < 1
    || value.occurrences.length > 35
    || !Array.isArray(value.pantry)
  ) {
    return null;
  }

  const locality = boundedString(value.locality, 160);
  if (locality === null) return null;

  const occurrences: WeeklyPlanDraftOccurrence[] = [];
  for (const candidate of value.occurrences) {
    const decoded = occurrence(candidate);
    if (decoded === null) return null;
    occurrences.push(decoded);
  }

  const pantry: WeeklyPlanDraftPantryItem[] = [];
  for (const candidate of value.pantry) {
    const decoded = pantryItem(candidate);
    if (decoded === null) return null;
    pantry.push(decoded);
  }

  return { version: 1, locality, occurrences, pantry };
}

export function decodeWeeklyPlanDraft(raw: string): WeeklyPlanDraftV1 | null {
  try {
    return decodeValue(JSON.parse(raw));
  } catch {
    return null;
  }
}

export function readWeeklyPlanDraft(storage: Storage): DraftReadResult {
  let raw: string | null;
  try {
    raw = storage.getItem(WEEKLY_PLAN_DRAFT_STORAGE_KEY);
  } catch {
    return { kind: "unavailable" };
  }

  if (raw === null) return { kind: "ready", draft: null };

  const draft = decodeWeeklyPlanDraft(raw);
  if (draft !== null) return { kind: "ready", draft };

  try {
    storage.removeItem(WEEKLY_PLAN_DRAFT_STORAGE_KEY);
  } catch {
    // The corrupt value is still ignored. A later normal write can prove whether storage is usable.
  }
  return { kind: "ready", draft: null };
}

export function writeWeeklyPlanDraft(storage: Storage, value: unknown): DraftWriteResult {
  const draft = decodeValue(value);
  if (draft === null) return { kind: "unavailable" };

  try {
    storage.setItem(WEEKLY_PLAN_DRAFT_STORAGE_KEY, JSON.stringify(draft));
    return { kind: "saved" };
  } catch {
    return { kind: "unavailable" };
  }
}

export function removeWeeklyPlanDraft(storage: Storage): DraftRemoveResult {
  try {
    storage.removeItem(WEEKLY_PLAN_DRAFT_STORAGE_KEY);
    return { kind: "removed" };
  } catch {
    return { kind: "unavailable" };
  }
}
