import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:warehouse_pda_app/main.dart';

void main() {
  testWidgets('renders PDA receiving workflow', (WidgetTester tester) async {
    await tester.pumpWidget(const WarehousePdaApp());

    expect(find.text('Warehouse PDA'), findsOneWidget);
    expect(find.text('작업 대기열'), findsOneWidget);
    expect(find.text('스캔 입력'), findsOneWidget);

    await tester.scrollUntilVisible(
      find.text('입고 검수'),
      300,
      scrollable: find.byType(Scrollable).first,
    );
    expect(find.text('입고 검수'), findsOneWidget);

    await tester.scrollUntilVisible(
      find.text('로케이션 적치'),
      300,
      scrollable: find.byType(Scrollable).first,
    );
    expect(find.text('로케이션 적치'), findsOneWidget);
  });
}
