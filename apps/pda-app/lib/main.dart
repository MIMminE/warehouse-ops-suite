import 'package:flutter/material.dart';

void main() {
  runApp(const WarehousePdaApp());
}

class WarehousePdaApp extends StatelessWidget {
  const WarehousePdaApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Warehouse PDA',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF1B5E57)),
        useMaterial3: true,
        scaffoldBackgroundColor: const Color(0xFFEFF3F4),
      ),
      home: const PdaReceivingHomePage(),
    );
  }
}

class ReceivingTask {
  const ReceivingTask({
    required this.no,
    required this.client,
    required this.warehouse,
    required this.sku,
    required this.product,
    required this.expectedQuantity,
    required this.inspectedQuantity,
    required this.putawayQuantity,
    required this.recommendedLocations,
    required this.status,
  });

  final String no;
  final String client;
  final String warehouse;
  final String sku;
  final String product;
  final int expectedQuantity;
  final int inspectedQuantity;
  final int putawayQuantity;
  final List<String> recommendedLocations;
  final String status;

  int get waitingPutawayQuantity => inspectedQuantity - putawayQuantity;
}

class ScanEvent {
  const ScanEvent({
    required this.time,
    required this.type,
    required this.barcode,
    required this.result,
  });

  final String time;
  final String type;
  final String barcode;
  final String result;
}

class PdaReceivingHomePage extends StatefulWidget {
  const PdaReceivingHomePage({super.key});

  @override
  State<PdaReceivingHomePage> createState() => _PdaReceivingHomePageState();
}

class _PdaReceivingHomePageState extends State<PdaReceivingHomePage> {
  final TextEditingController _barcodeController = TextEditingController();
  final TextEditingController _inspectQuantityController =
      TextEditingController(text: '24');
  final TextEditingController _putawayQuantityController =
      TextEditingController(text: '24');

  final List<ReceivingTask> _tasks = const [
    ReceivingTask(
      no: 'RCV-20260518-001',
      client: 'A 고객사',
      warehouse: '수도권 1센터',
      sku: 'SKU-4012',
      product: 'Basic Tee / Black',
      expectedQuantity: 120,
      inspectedQuantity: 96,
      putawayQuantity: 72,
      recommendedLocations: ['A-01-03', 'A-01-02'],
      status: '검수중',
    ),
    ReceivingTask(
      no: 'RCV-20260518-003',
      client: 'A 고객사',
      warehouse: '부산 2센터',
      sku: 'SKU-1024',
      product: 'Slim Bottle / Clear',
      expectedQuantity: 240,
      inspectedQuantity: 210,
      putawayQuantity: 120,
      recommendedLocations: ['C-04-05', 'C-03-01'],
      status: '적치중',
    ),
    ReceivingTask(
      no: 'RCV-20260519-004',
      client: 'C 고객사',
      warehouse: '수도권 1센터',
      sku: 'SKU-7780',
      product: 'Pouch Set / Gray',
      expectedQuantity: 160,
      inspectedQuantity: 0,
      putawayQuantity: 0,
      recommendedLocations: ['D-01-02'],
      status: '입고예정',
    ),
  ];

  final List<ScanEvent> _events = [
    const ScanEvent(
      time: '10:31',
      type: '로케이션 스캔',
      barcode: 'A-01-03',
      result: '적치 72개',
    ),
    const ScanEvent(
      time: '10:19',
      type: '상품 검수',
      barcode: 'SKU-4012',
      result: '96개 검수',
    ),
    const ScanEvent(
      time: '10:12',
      type: '입고번호 스캔',
      barcode: 'RCV-20260518-001',
      result: '성공',
    ),
  ];

  int _selectedIndex = 0;
  String _scanMode = '입고번호';
  String _selectedLocation = 'A-01-03';
  bool _offlineQueueEnabled = false;

  ReceivingTask get _selectedTask => _tasks[_selectedIndex];

  @override
  void dispose() {
    _barcodeController.dispose();
    _inspectQuantityController.dispose();
    _putawayQuantityController.dispose();
    super.dispose();
  }

  void _appendScanEvent(String type, String barcode, String result) {
    setState(() {
      _events.insert(
        0,
        ScanEvent(
          time: TimeOfDay.now().format(context),
          type: type,
          barcode: barcode,
          result: result,
        ),
      );
      _barcodeController.clear();
    });
  }

  void _handleScanSubmit() {
    final barcode = _barcodeController.text.trim();
    if (barcode.isEmpty) {
      return;
    }

    final task = _selectedTask;
    if (_scanMode == '입고번호') {
      final result = barcode == task.no ? '작업 시작' : '입고번호 불일치';
      _appendScanEvent('입고번호 스캔', barcode, result);
      return;
    }
    if (_scanMode == 'SKU') {
      final result = barcode == task.sku ? '상품 확인' : '상품 불일치';
      _appendScanEvent('상품 스캔', barcode, result);
      return;
    }

    final result =
        task.recommendedLocations.contains(barcode) ? '로케이션 확인' : '추천 외 로케이션';
    _appendScanEvent('로케이션 스캔', barcode, result);
  }

  void _confirmInspection() {
    _appendScanEvent(
      '검수 수량 확정',
      _selectedTask.sku,
      '${_inspectQuantityController.text}개 검수',
    );
  }

  void _confirmPutaway() {
    _appendScanEvent(
      '적치 완료',
      _selectedLocation,
      '${_putawayQuantityController.text}개 적치',
    );
  }

  @override
  Widget build(BuildContext context) {
    final task = _selectedTask;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Warehouse PDA'),
        actions: [
          Padding(
            padding: const EdgeInsets.only(right: 12),
            child: Center(
              child: Text(
                _offlineQueueEnabled ? '오프라인 큐 2건' : '동기화 정상',
                style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w600),
              ),
            ),
          ),
        ],
      ),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            _WorkerHeader(
              worker: '한지훈',
              device: 'PDA-01',
              warehouse: task.warehouse,
              offlineQueueEnabled: _offlineQueueEnabled,
              onToggleOfflineQueue: (value) {
                setState(() => _offlineQueueEnabled = value);
              },
            ),
            const SizedBox(height: 12),
            _SectionCard(
              title: '작업 대기열',
              trailing: '${_tasks.length}건',
              child: Column(
                children: [
                  for (var index = 0; index < _tasks.length; index++)
                    _TaskTile(
                      task: _tasks[index],
                      selected: index == _selectedIndex,
                      onTap: () {
                        setState(() {
                          _selectedIndex = index;
                          _selectedLocation =
                              _tasks[index].recommendedLocations.first;
                        });
                      },
                    ),
                ],
              ),
            ),
            const SizedBox(height: 12),
            _SectionCard(
              title: '스캔 입력',
              trailing: _scanMode,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  SegmentedButton<String>(
                    segments: const [
                      ButtonSegment(value: '입고번호', label: Text('입고')),
                      ButtonSegment(value: 'SKU', label: Text('SKU')),
                      ButtonSegment(value: '로케이션', label: Text('LOC')),
                    ],
                    selected: {_scanMode},
                    onSelectionChanged: (selection) {
                      setState(() => _scanMode = selection.first);
                    },
                  ),
                  const SizedBox(height: 12),
                  TextField(
                    controller: _barcodeController,
                    textInputAction: TextInputAction.done,
                    onSubmitted: (_) => _handleScanSubmit(),
                    decoration: InputDecoration(
                      labelText: '바코드 스캔 또는 수동 입력',
                      hintText: _hintForScanMode(task),
                      prefixIcon: const Icon(Icons.qr_code_scanner),
                      border: const OutlineInputBorder(),
                    ),
                  ),
                  const SizedBox(height: 10),
                  FilledButton.icon(
                    onPressed: _handleScanSubmit,
                    icon: const Icon(Icons.check),
                    label: const Text('스캔 확인'),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 12),
            _SectionCard(
              title: '입고 검수',
              trailing: task.status,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  _SkuSummary(task: task),
                  const SizedBox(height: 12),
                  TextField(
                    controller: _inspectQuantityController,
                    keyboardType: TextInputType.number,
                    decoration: const InputDecoration(
                      labelText: '검수 수량',
                      prefixIcon: Icon(Icons.inventory_2_outlined),
                      border: OutlineInputBorder(),
                    ),
                  ),
                  const SizedBox(height: 10),
                  FilledButton.icon(
                    onPressed: _confirmInspection,
                    icon: const Icon(Icons.fact_check_outlined),
                    label: const Text('검수 수량 확정'),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 12),
            _SectionCard(
              title: '로케이션 적치',
              trailing: '미적치 ${task.waitingPutawayQuantity}',
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  DropdownButtonFormField<String>(
                    key: ValueKey(_selectedLocation),
                    initialValue: _selectedLocation,
                    items: task.recommendedLocations
                        .map(
                          (location) => DropdownMenuItem(
                            value: location,
                            child: Text(location),
                          ),
                        )
                        .toList(),
                    onChanged: (value) {
                      if (value != null) {
                        setState(() => _selectedLocation = value);
                      }
                    },
                    decoration: const InputDecoration(
                      labelText: '추천 로케이션',
                      prefixIcon: Icon(Icons.location_on_outlined),
                      border: OutlineInputBorder(),
                    ),
                  ),
                  const SizedBox(height: 10),
                  TextField(
                    controller: _putawayQuantityController,
                    keyboardType: TextInputType.number,
                    decoration: const InputDecoration(
                      labelText: '적치 수량',
                      prefixIcon: Icon(Icons.move_to_inbox_outlined),
                      border: OutlineInputBorder(),
                    ),
                  ),
                  const SizedBox(height: 10),
                  FilledButton.icon(
                    onPressed: _confirmPutaway,
                    icon: const Icon(Icons.warehouse_outlined),
                    label: const Text('로케이션 적치 완료'),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 12),
            _SectionCard(
              title: 'PDA 스캔 이벤트',
              trailing: '${_events.length}건',
              child: Column(
                children: [
                  for (final event in _events.take(6)) _ScanEventTile(event: event),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  String _hintForScanMode(ReceivingTask task) {
    if (_scanMode == '입고번호') {
      return task.no;
    }
    if (_scanMode == 'SKU') {
      return task.sku;
    }
    return task.recommendedLocations.join(', ');
  }
}

class _WorkerHeader extends StatelessWidget {
  const _WorkerHeader({
    required this.worker,
    required this.device,
    required this.warehouse,
    required this.offlineQueueEnabled,
    required this.onToggleOfflineQueue,
  });

  final String worker;
  final String device;
  final String warehouse;
  final bool offlineQueueEnabled;
  final ValueChanged<bool> onToggleOfflineQueue;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            const CircleAvatar(child: Icon(Icons.person)),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    '$worker / $device',
                    style: Theme.of(context).textTheme.titleMedium,
                  ),
                  const SizedBox(height: 2),
                  Text(warehouse),
                ],
              ),
            ),
            Switch(
              value: offlineQueueEnabled,
              onChanged: onToggleOfflineQueue,
            ),
          ],
        ),
      ),
    );
  }
}

class _SectionCard extends StatelessWidget {
  const _SectionCard({
    required this.title,
    required this.trailing,
    required this.child,
  });

  final String title;
  final String trailing;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Row(
              children: [
                Expanded(
                  child: Text(
                    title,
                    style: Theme.of(context).textTheme.titleMedium,
                  ),
                ),
                Chip(label: Text(trailing)),
              ],
            ),
            const SizedBox(height: 12),
            child,
          ],
        ),
      ),
    );
  }
}

class _TaskTile extends StatelessWidget {
  const _TaskTile({
    required this.task,
    required this.selected,
    required this.onTap,
  });

  final ReceivingTask task;
  final bool selected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Card.outlined(
      color: selected ? const Color(0xFFE3F1EE) : null,
      child: ListTile(
        onTap: onTap,
        title: Text(task.no),
        subtitle: Text('${task.client} / ${task.sku} / ${task.product}'),
        trailing: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.end,
          children: [
            Text(task.status),
            Text('미적치 ${task.waitingPutawayQuantity}'),
          ],
        ),
      ),
    );
  }
}

class _SkuSummary extends StatelessWidget {
  const _SkuSummary({required this.task});

  final ReceivingTask task;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        border: Border.all(color: Theme.of(context).colorScheme.outlineVariant),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(task.sku, style: Theme.of(context).textTheme.titleMedium),
          const SizedBox(height: 2),
          Text(task.product),
          const SizedBox(height: 10),
          LinearProgressIndicator(
            value: task.expectedQuantity == 0
                ? 0
                : task.inspectedQuantity / task.expectedQuantity,
          ),
          const SizedBox(height: 8),
          Text(
            '예정 ${task.expectedQuantity} / 검수 ${task.inspectedQuantity} / 적치 ${task.putawayQuantity}',
          ),
        ],
      ),
    );
  }
}

class _ScanEventTile extends StatelessWidget {
  const _ScanEventTile({required this.event});

  final ScanEvent event;

  @override
  Widget build(BuildContext context) {
    return ListTile(
      dense: true,
      contentPadding: EdgeInsets.zero,
      title: Text(event.type),
      subtitle: Text('${event.barcode} / ${event.result}'),
      trailing: Text(event.time),
    );
  }
}
