# Portfolio Summary

## Project

Warehouse Ops Suite is a portfolio WMS project that connects admin web operations, PDA field workflows, a local print agent, and a DPS device protocol simulator.

## Clean-Room Principle

The project is based on generalized WMS/LMS domain learning. It does not reuse company code, private schemas, API contracts, customer data, logs, or internal names.

## Main Story

The first product slice focuses on outbound picking and printing:

1. An operator creates an outbound wave.
2. The system assigns picking tasks.
3. The DPS agent simulates cell lighting and picking events.
4. A PDA worker confirms picked quantities.
5. The backend creates invoice data.
6. The PDF renderer creates a document.
7. The print agent sends the document to a local printer adapter.
8. The admin web tracks print success or failure.

