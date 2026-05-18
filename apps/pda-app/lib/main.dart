import 'package:flutter/material.dart';

void main() {
  runApp(const WarehousePdaApp());
}

class WarehousePdaApp extends StatelessWidget {
  const WarehousePdaApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Warehouse PDA',
      theme: ThemeData(useMaterial3: true),
      home: const Scaffold(
        body: Center(
          child: Text('Warehouse PDA shell'),
        ),
      ),
    );
  }
}

