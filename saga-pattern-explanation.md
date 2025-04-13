# Pattern Saga Coreografico: Guida Completa

## Cos'è il Pattern Saga

Il pattern Saga è una soluzione per gestire transazioni distribuite in architetture a microservizi. Invece di utilizzare transazioni ACID tradizionali (che sono difficili da implementare in sistemi distribuiti), una "saga" è una sequenza di transazioni locali dove ogni transazione aggiorna i dati all'interno di un singolo servizio, e pubblica eventi per innescare la transazione successiva.

Se una transazione fallisce, vengono eseguite transazioni di compensazione per annullare le modifiche effettuate dalle transazioni precedenti.

## Approccio Coreografico vs Orchestrato

Ci sono due principali varianti del pattern Saga:

1. **Saga Coreografica**: ogni servizio pubblica eventi quando avvengono cambiamenti di stato, e altri servizi si sottoscrivono a questi eventi reagendo di conseguenza. Non c'è un coordinatore centrale.

2. **Saga Orchestrata**: un servizio centralizzato (orchestratore) definisce i passaggi della transazione e coordina l'esecuzione.

## Implementazione di una Saga Coreografica

Una tipica implementazione Saga si compone di più microservizi che comunicano tramite eventi. Ad esempio, in un sistema e-commerce:

1. **OrderService**: gestisce gli ordini
2. **PaymentService**: gestisce i pagamenti
3. **ShippingService**: gestisce le spedizioni

Questi servizi comunicano tra loro attraverso **eventi** scambiati tramite un message broker come Apache Kafka.

### Flusso principale (Happy Path):

1. Un cliente crea un ordine tramite `OrderService`
2. `OrderService` salva l'ordine con stato "CREATED" e pubblica un evento `OrderCreatedEvent`
3. `PaymentService` è in ascolto per eventi `OrderCreatedEvent`, elabora il pagamento e pubblica un evento `PaymentStatusChangedEvent`
4. `ShippingService` è in ascolto per eventi `PaymentStatusChangedEvent` (con stato "SUCCEEDED"), crea la spedizione e pubblica un evento `ShipmentStatusChangedEvent`
5. `OrderService` è in ascolto per eventi `ShipmentStatusChangedEvent` e aggiorna lo stato dell'ordine in base allo stato della spedizione

### Gestione dei fallimenti:

Se un servizio fallisce in qualsiasi punto, può pubblicare un evento di fallimento che innesca azioni di compensazione negli altri servizi. Ad esempio:

- Se il pagamento fallisce, `PaymentService` pubblica un evento `PaymentStatusChangedEvent` con stato "FAILED"
- `OrderService` risponde aggiornando lo stato dell'ordine a "PAYMENT_FAILED"
- `ShippingService` non tenterà di creare una spedizione per questo ordine

## Vantaggi dell'approccio Coreografico

1. **Disaccoppiamento**: I servizi non sono a conoscenza l'uno dell'altro, conoscono solo gli eventi a cui sono interessati
2. **Scalabilità**: Ogni servizio può scalare indipendentemente
3. **Autonomia**: Ogni team può sviluppare, testare e deployare il proprio servizio in modo indipendente
4. **Resistenza ai guasti**: Il fallimento di un servizio non porta necessariamente al fallimento dell'intero sistema

## Struttura del codice in un'implementazione tipica

Ogni servizio segue una struttura simile:

- **Entità**: classi che rappresentano gli oggetti di dominio (`Order`, `Payment`, `Shipment`)
- **Eventi**: classi che rappresentano cambiamenti di stato (`OrderCreatedEvent`, `PaymentStatusChangedEvent`)
- **Stati**: enum che rappresentano i possibili stati di un'entità (`OrderStatus`, `PaymentStatus`)
- **Repository**: interfacce per l'interazione con il database
- **Service**: classi che contengono la logica di business e i listener per gli eventi
- **Controller**: endpoint REST per interagire con i servizi

## Problemi risolti dal pattern Saga

Il pattern Saga risolve specificamente diversi problemi critici nei sistemi distribuiti:

### 1. L'impossibilità di transazioni distribuite ACID

In un sistema monolitico, le transazioni ACID (Atomicità, Coerenza, Isolamento, Durabilità) sono gestite dal database. Quando i dati sono distribuiti tra più servizi con database separati, le transazioni ACID tradizionali diventano impraticabili perché:

- Il two-phase commit (2PC) tradizionalmente usato per transazioni distribuite introduce problemi di performance
- Il 2PC richiede che tutti i servizi siano disponibili simultaneamente, riducendo la disponibilità del sistema
- Le transazioni distribuite bloccano le risorse per lunghi periodi

### 2. La consistenza dei dati tra servizi

Quando un'operazione di business coinvolge modifiche a dati in più servizi, è necessario garantire che tutti i servizi alla fine convergano a uno stato coerente, anche in presenza di fallimenti parziali.

### 3. La gestione dei fallimenti parziali

In un'architettura distribuita, alcuni servizi possono fallire mentre altri hanno già completato le loro operazioni. Senza un meccanismo come Saga, sarebbe difficile ripristinare il sistema a uno stato coerente.

### 4. Il coordinamento di transazioni a lungo termine

Alcune operazioni di business possono richiedere molto tempo (minuti, ore o giorni). Le transazioni ACID tradizionali non sono adatte a scenari di lunga durata.

### 5. L'accoppiamento tra servizi

Senza un pattern come Saga, l'implementazione di logiche di business che attraversano più servizi richiederebbe un forte accoppiamento tra i servizi stessi.

## Come Saga risolve questi problemi

1. **Consistenza eventuale**: Invece di consistenza immediata, Saga garantisce che il sistema raggiungerà alla fine uno stato coerente.

2. **Transazioni locali**: Ogni servizio esegue transazioni locali ACID, mantenendo l'integrità dei dati all'interno del proprio dominio.

3. **Azioni di compensazione**: Se una fase della Saga fallisce, vengono eseguite azioni di compensazione per annullare gli effetti delle fasi precedenti, riportando il sistema a uno stato coerente.

4. **Disaccoppiamento dei servizi**: Con l'approccio coreografico, i servizi comunicano solo tramite eventi, senza dipendenze dirette.

5. **Resilienza**: Il sistema può continuare a funzionare anche quando alcuni servizi sono temporaneamente non disponibili.

6. **Scalabilità**: I servizi possono essere scalati indipendentemente senza impattare le transazioni distribuite.

## Aree di miglioramento in un'implementazione Saga

Per rendere un'implementazione Saga più robusta, è importante considerare:

1. **Pattern Outbox**: Garantisce che gli eventi vengano sempre pubblicati anche in caso di fallimenti, utilizzando una tabella di "outbox" nel database.

2. **Idempotenza**: Aggiungere identificatori univoci alle transazioni e meccanismi per rilevare duplicati.

3. **Timeout e strategie di retry**: Per gestire scenari in cui i servizi non rispondono.

4. **Transazioni di compensazione complete**: Definire chiaramente come ripristinare lo stato precedente per ogni tipo di fallimento.

5. **Monitoraggio centralizzato**: Per tracciare lo stato complessivo delle saga.

## Contesti applicativi per il pattern Saga Coreografico

Il pattern Saga Coreografico è particolarmente utile in diversi contesti:

### 1. Sistemi finanziari e bancari
- Trasferimenti di denaro tra conti
- Elaborazione di prestiti
- Elaborazione di pagamenti

### 2. Sistemi di prenotazione e viaggi
- Prenotazioni di voli e hotel
- Pacchetti di viaggio
- Sistemi di trasporto multimodali

### 3. Sanità e servizi medici
- Ciclo di approvazione assicurativa
- Coordinamento di appuntamenti
- Gestione delle prescrizioni

### 4. Supply chain e logistica
- Gestione dell'inventario cross-warehouse
- Elaborazione di ordini multifornitore
- Gestione del ciclo di vita della spedizione

### 5. Telecomunicazioni
- Attivazione di servizi
- Portabilità del numero
- Gestione delle offerte bundle

### 6. Industria 4.0 e sistemi IoT
- Processi produttivi distribuiti
- Manutenzione predittiva
- Sistemi di energia intelligenti

## Confronto con altri pattern

### Saga vs Chain of Responsibility

Sebbene ci siano alcune similitudini concettuali tra il pattern Saga e il pattern Chain of Responsibility, servono a scopi fondamentalmente diversi:

#### Somiglianze
- Entrambi i pattern coinvolgono una sequenza di handler/passaggi
- Entrambi distribuiscono responsabilità tra componenti diversi
- In entrambi i casi, un componente "passa" un'attività al successivo

#### Differenze fondamentali
1. **Scopo principale**:
   - **Chain of Responsibility**: Disaccoppia mittenti e destinatari di una richiesta
   - **Saga**: Gestisce transazioni distribuite tra servizi indipendenti

2. **Contesto di applicazione**:
   - **Chain of Responsibility**: Pattern di design a livello di oggetto
   - **Saga**: Pattern architetturale per sistemi distribuiti

3. **Flusso di controllo**:
   - **Chain of Responsibility**: Sequenziale e sincrono
   - **Saga**: Orchestrato o coreografato, spesso asincrono

4. **Gestione dei fallimenti**:
   - **Chain of Responsibility**: Non include meccanismi intrinseci di compensazione
   - **Saga**: Include esplicitamente azioni di compensazione

5. **Stato**:
   - **Chain of Responsibility**: Generalmente stateless
   - **Saga**: Intrinsecamente stateful

## Conclusione

Il pattern Saga, e in particolare l'approccio Coreografico, è una soluzione potente per gestire transazioni distribuite in architetture a microservizi. Consente di mantenere la consistenza dei dati in un sistema distribuito, gestendo efficacemente i fallimenti e promuovendo il disaccoppiamento tra i servizi.

La sua implementazione richiede un'attenta progettazione, soprattutto per quanto riguarda le strategie di compensazione e il monitoraggio delle transazioni distribuite, ma i benefici in termini di scalabilità, disponibilità e autonomia dei team di sviluppo lo rendono una scelta eccellente per molti contesti applicativi moderni.
