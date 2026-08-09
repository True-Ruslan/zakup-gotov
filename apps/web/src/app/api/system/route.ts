export const dynamic = "force-dynamic";

export async function GET() {
  const apiBaseUrl = process.env.API_BASE_URL;

  if (!apiBaseUrl) {
    return Response.json({ status: "UNAVAILABLE" }, { status: 503 });
  }

  try {
    const response = await fetch(new URL("/api/v1/system", apiBaseUrl), {
      cache: "no-store",
    });
    const body = await response.text();

    return new Response(body, {
      status: response.status,
      headers: {
        "content-type": response.headers.get("content-type") ?? "application/json",
      },
    });
  } catch {
    return Response.json({ status: "UNAVAILABLE" }, { status: 503 });
  }
}
