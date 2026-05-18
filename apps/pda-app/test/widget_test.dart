import 'package:flutter_test/flutter_test.dart';

import 'package:warehouse_pda_app/main.dart';

void main() {
  testWidgets('renders PDA shell', (WidgetTester tester) async {
    await tester.pumpWidget(const WarehousePdaApp());

    expect(find.text('Warehouse PDA shell'), findsOneWidget);
  });
}
