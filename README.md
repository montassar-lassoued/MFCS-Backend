# MFCS Backend – IntraConnect Core Engine ⚙️

[![Java](https://img.shields.io/badge/Java-17%2B-007396?style=for-the-badge&logo=java&logoColor=white)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/Maven-3.6%2B-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)](https://maven.apache.org/)
[![Status](https://img.shields.io/badge/Status-Early_Development-orange?style=for-the-badge)]()

Die **IntraConnect Core Engine** ist das leistungsstarke Rückgrat des Material Flow Control Systems (MFCS). Diese Java-basierte Middleware fungiert als zentrale Schaltstelle zwischen der physischen Hardware (Fördertechnik, Regalbediengeräte) und den übergeordneten Softwaresystemen.

---

## 🏗️ Architektur & Konzepte

Das System ist als **Multi-Module Maven Project** konzipiert, was eine strikte Trennung von Geschäftslogik, Datenpersistenz und Kommunikation ermöglicht. Da sich das Projekt in einer frühen Phase befindet, liegt der Fokus auf einer hochflexiblen und erweiterbaren Basis.

### Kern-Säulen der Entwicklung:
* **Modulare Skalierbarkeit:** Die Architektur erlaubt es, Funktionsbereiche unabhängig voneinander zu erweitern und zu modernisieren, ohne die Stabilität des Gesamtsystems zu gefährden.
* **Industrielle Kommunikation:** Implementierung robuster Echtzeit-Konnektivität über TCP- und UDP-Protokolle zur Anbindung von speicherprogrammierbaren Steuerungen (SPS) und Sensoren.
* **Effiziente Logistik-Logik:** Entwicklung von Algorithmen für präzise Warenbewegungen, Routing-Entscheidungen und Bestandsmanagement in automatisierten Lagern.
* **Datenintegrität:** Fokus auf transaktionale Sicherheit bei komplexen Lagerprozessen zur Vermeidung von Systeminkonsistenzen.

---

## ⚙️ Konfiguration & Steuerung

Ein zentrales Merkmal des Systems ist die Steuerung über eine umfassende XML-Struktur (`App_Config.xml`). Um die Komplexität beherrschbar zu machen, wird diese Konfiguration visuell generiert:

👉 **Konfigurations-Tool:** Die gesamte Anlagenlogik und Netzwerk-Topologie wird über den [IntraConnect-Configurator](https://github.com/montassar-lassoued/IntraConnect-Configurator) geplant und validiert exportiert.

---

## 🛠️ Modernisierungs-Roadmap

Das Projekt wird aktiv weiterentwickelt und modernisiert. Geplante Meilensteine sind:

- [ ] **Modul-Expansion:** Kontinuierlicher Ausbau der Logik-Module für komplexere Materialfluss-Szenarien.
- [ ] **Next-Gen API:** Aufbau eines REST-Layers zur nahtlosen Echtzeit-Kommunikation mit dem [MFCS-Frontend](https://github.com/montassar-lassoued/MFCS-Frontend).
- [ ] **Core-Refactoring:** Laufende Optimierung des Codes auf Basis aktueller Java-Enterprise-Standards.
- [ ] **Containerisierung:** Vorbereitung für Docker-Umgebungen zur vereinfachten Bereitstellung und Skalierung.

---

## 🚀 Installation & Build

1. **Voraussetzungen:**
   - Java Development Kit (JDK) 17 oder höher.
   - Apache Maven 3.6+.

2. **Projekt bauen:**
   ```bash
   git clone [https://github.com/montassar-lassoued/MFCS-Backend.git](https://github.com/montassar-lassoued/MFCS-Backend.git)
   cd MFCS-Backend
   mvn clean install
