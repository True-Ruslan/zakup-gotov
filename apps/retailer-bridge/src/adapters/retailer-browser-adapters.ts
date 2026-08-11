import { perekrestokBrowserAdapter } from "./perekrestok-browser-adapter";
import { pyaterochkaBrowserAdapter } from "./pyaterochka-browser-adapter";

export const retailerBrowserAdapters = [
  perekrestokBrowserAdapter,
  pyaterochkaBrowserAdapter,
] as const;
