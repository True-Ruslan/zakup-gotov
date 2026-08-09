import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Закуп готов",
  description:
    "Сервис для сравнения полной продуктовой корзины по актуальным ценам и наличию в магазинах.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ru" className="h-full antialiased">
      <body className="min-h-full flex flex-col">{children}</body>
    </html>
  );
}
