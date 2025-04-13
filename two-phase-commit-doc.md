# Two-Phase Commit nei Sistemi Distribuiti

## Introduzione al Two-Phase Commit (2PC)

Il Two-Phase Commit (2PC) è un protocollo di coordinamento utilizzato nelle transazioni distribuite per garantire che tutti i partecipanti alla transazione eseguano il commit o il rollback in modo coerente. È stato progettato per mantenere l'atomicità delle transazioni in un contesto distribuito.

## Come funziona il Two-Phase Commit

Il protocollo 2PC si articola in due fasi distinte:

### Fase 1: Preparazione

1. Il coordinatore invia un messaggio "prepare" a tutti i partecipanti
2. Ogni partecipante esegue le operazioni della transazione e le registra nel suo log
3. Ogni partecipante risponde con "ready" se può eseguire il commit, o "abort" se ci sono problemi
4. I partecipanti bloccano le risorse necessarie fino al completamento della transazione

### Fase 2: Commit/Rollback

1. Se tutti i partecipanti rispondono "ready", il coordinatore invia un messaggio di "commit"
2. Se uno o più partecipanti rispondono "abort", il coordinatore invia un messaggio di "rollback"
3. I partecipanti completano l'operazione in base al comando ricevuto e rilasciano le risorse

## Implementazione in ambienti Java/Spring

Per implementare il 2PC in un'applicazione Spring Boot, si può utilizzare:

1. **Java Transaction API (JTA)** con un'implementazione come Atomikos o Narayana
2. **Spring Transaction Management** con supporto XA

### Esempio di configurazione con Atomikos

```java
@Configuration
@EnableTransactionManagement
public class TransactionConfig {

    @Bean(initMethod = "init", destroyMethod = "close")
    public UserTransactionManager userTransactionManager() {
        UserTransactionManager manager = new UserTransactionManager();
        manager.setForceShutdown(true);
        return manager;
    }

    @Bean
    public UserTransaction userTransaction() throws SystemException {
        UserTransactionImp userTransactionImp = new UserTransactionImp();
        userTransactionImp.setTransactionTimeout(300);
        return userTransactionImp;
    }

    @Bean
    public JtaTransactionManager transactionManager() throws SystemException {
        JtaTransactionManager manager = new JtaTransactionManager();
        manager.setUserTransaction(userTransaction());
        manager.setTransactionManager(userTransactionManager());
        manager.setAllowCustomIsolationLevels(true);
        return manager;
    }
}
```

## Pattern che utilizzano il Two-Phase Commit

Il 2PC viene impiegato principalmente nei seguenti pattern architetturali:

1. **Distributed Transaction Pattern**
   - Pattern fondamentale per garantire l'atomicità delle transazioni tra più risorse distribuite
   - Usato quando operazioni su database, code messaggi e servizi devono essere trattate come un'unica unità di lavoro

2. **Saga Pattern (variante coordinata)**
   - Mentre molte implementazioni moderne delle Saga usano compensazioni, alcune versioni utilizzano 2PC per coordinare i passaggi critici
   - Permette di gestire transazioni di lunga durata tra servizi

3. **Resource Manager Pattern**
   - Gestisce l'accesso a risorse distribuite (database, code, ecc.)
   - Utilizza 2PC per garantire la consistenza tra le diverse risorse

4. **Transaction Coordinator Pattern**
   - Implementa un componente centralizzato che coordina transazioni distribuite
   - Il coordinatore gestisce entrambe le fasi del 2PC

5. **Service Integration Pattern**
   - Quando si integrano servizi eterogenei che devono partecipare a una transazione atomica

6. **XA Transaction Pattern**
   - Basato sullo standard XA per transazioni distribuite
   - Implementato in molti server applicativi Java EE/Jakarta EE

7. **Reservation Pattern** (con 2PC)
   - Implementa prenotazioni temporanee di risorse nella prima fase
   - Conferma o annulla le prenotazioni nella seconda fase

8. **Compensating Transaction Pattern** (evoluzione)
   - Mentre questo pattern è spesso un'alternativa al 2PC, in alcuni casi ibridi il 2PC viene usato per atomicità locale con compensazioni per rollback distribuiti

9. **Resource Locking Pattern**
   - Implementa il blocco delle risorse distribuite durante la prima fase del commit

10. **Transactional Microservices Pattern**
    - In alcuni casi (non frequenti) in cui è necessaria una consistenza forte tra microservizi

## Esempio concreto: Trasferimento bancario tra due banche

Ecco un esempio di utilizzo del Two-Phase Commit in un sistema bancario per un trasferimento di denaro tra due conti su database diversi:

### Implementazione con Spring e JTA

```java
@Service
public class TransferService {
    
    @Autowired
    private AccountRepositoryBankA bankARepository;
    
    @Autowired
    private AccountRepositoryBankB bankBRepository;
    
    @Autowired
    private TransactionLogRepository logRepository;
    
    @Transactional // Questa annotazione abiliterà il 2PC se configurato con JTA
    public void transferMoney(String sourceAccountId, String targetAccountId, BigDecimal amount) {
        // 1. Verifica disponibilità fondi
        Account sourceAccount = bankARepository.findById(sourceAccountId)
            .orElseThrow(() -> new AccountNotFoundException("Account non trovato"));
            
        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Fondi insufficienti");
        }
        
        // 2. Preleva dal conto di origine (Banca A)
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
        bankARepository.save(sourceAccount);
        
        // 3. Deposita nel conto di destinazione (Banca B)
        Account targetAccount = bankBRepository.findById(targetAccountId)
            .orElseThrow(() -> new AccountNotFoundException("Account destinazione non trovato"));
        targetAccount.setBalance(targetAccount.getBalance().add(amount));
        bankBRepository.save(targetAccount);
        
        // 4. Registra la transazione nel log
        TransactionLog log = new TransactionLog();
        log.setSourceAccountId(sourceAccountId);
        log.setTargetAccountId(targetAccountId);
        log.setAmount(amount);
        log.setTimestamp(LocalDateTime.now());
        logRepository.save(log);
    }
}
```

### Configurazione JTA per abilitare il 2PC

```java
@Configuration
@EnableTransactionManagement
public class JtaConfig {

    @Bean(initMethod = "init", destroyMethod = "close")
    public AtomikosDataSourceBean dataSourceBankA() {
        AtomikosDataSourceBean dataSource = new AtomikosDataSourceBean();
        dataSource.setUniqueResourceName("bankA");
        dataSource.setXaDataSourceClassName("org.postgresql.xa.PGXADataSource");
        Properties props = new Properties();
        props.put("url", "jdbc:postgresql://serverA:5432/bankA");
        props.put("user", "postgres");
        props.put("password", "password");
        dataSource.setXaProperties(props);
        return dataSource;
    }

    @Bean(initMethod = "init", destroyMethod = "close")
    public AtomikosDataSourceBean dataSourceBankB() {
        AtomikosDataSourceBean dataSource = new AtomikosDataSourceBean();
        dataSource.setUniqueResourceName("bankB");
        dataSource.setXaDataSourceClassName("org.postgresql.xa.PGXADataSource");
        Properties props = new Properties();
        props.put("url", "jdbc:postgresql://serverB:5432/bankB");
        props.put("user", "postgres");
        props.put("password", "password");
        dataSource.setXaProperties(props);
        return dataSource;
    }

    @Bean
    public JtaTransactionManager transactionManager() {
        UserTransactionManager userTransactionManager = new UserTransactionManager();
        UserTransaction userTransaction = new UserTransactionImp();
        return new JtaTransactionManager(userTransaction, userTransactionManager);
    }
}
```

### Cosa succede dietro le quinte (2PC in azione)

1. **Fase di preparazione**:
   - Il Transaction Manager contatta entrambi i Resource Manager (database di Banca A e Banca B)
   - Ogni database verifica che possa eseguire le operazioni (addebito e accredito)
   - Entrambi i database preparano le modifiche e bloccano le risorse
   - Ogni database risponde "ready" se è in grado di completare la transazione

2. **Fase di commit**:
   - Se entrambi i database rispondono "ready", il Transaction Manager ordina a entrambi di effettuare il commit
   - Se uno dei database risponde "abort", il Transaction Manager ordina a entrambi di effettuare il rollback
   - Entrambi i database eseguono l'operazione richiesta e rilasciano i blocchi

## Vantaggi e svantaggi del Two-Phase Commit

### Vantaggi:
- **Consistenza forte**: Garantisce atomicità delle transazioni distribuite
- **Isolamento**: Le modifiche non sono visibili fino al commit completo
- **Affidabilità**: Protocol ben collaudato con decenni di utilizzo in produzione
- **Integrità dei dati**: Previene inconsistenze tra sistemi integrati

### Svantaggi:
- **Prestazioni limitate**: Il protocollo è bloccante e richiede più round di comunicazione
- **Problema del singolo punto di fallimento**: Se il coordinatore fallisce, le transazioni possono restare bloccate
- **Scalabilità limitata**: Non scala bene all'aumentare dei partecipanti
- **Possibili deadlock**: Il blocco delle risorse può portare a situazioni di stallo
- **Complessità operativa**: Richiede configurazione e monitoraggio complessi

## Two-Phase Commit nei sistemi distribuiti vs microservizi

### 2PC nei sistemi distribuiti tradizionali

Il 2PC è storicamente utilizzato in:
- Sistemi bancari monolitici distribuiti
- Applicazioni enterprise con database federati
- Sistemi ERP che operano su più database
- Applicazioni SOA (Service-Oriented Architecture) con transazioni distribuite

In questi contesti, il 2PC è appropriato perché:
- La coerenza immediata è spesso un requisito non negoziabile
- Il numero di partecipanti alle transazioni è limitato e prevedibile
- L'infrastruttura è stabile e controllata
- I tempi di risposta possono essere sacrificati per la garanzia di consistenza

### Perché è problematico nei microservizi

Nei microservizi, il 2PC è generalmente evitato per diversi motivi:

1. **Accoppiamento temporale forte**
   - Il 2PC richiede che tutti i servizi siano disponibili simultaneamente
   - I microservizi sono progettati per essere indipendenti e resistenti ai guasti degli altri servizi

2. **Problemi di scalabilità**
   - Il blocco delle risorse durante la fase di preparazione limita la concorrenza
   - I microservizi devono poter scalare indipendentemente

3. **Single point of failure**
   - Il coordinatore della transazione diventa un potenziale punto di fallimento
   - Va contro il principio di resilienza distribuita dei microservizi

4. **Latenza elevata**
   - La comunicazione in due fasi aumenta significativamente la latenza
   - I microservizi moderni spesso priorizzano prestazioni e reattività

5. **Complessità operativa**
   - Gestire e monitorare transazioni 2PC tra microservizi è complesso
   - Il debug di problemi transazionali diventa estremamente difficile

## Alternative nei microservizi

Nell'architettura a microservizi si preferiscono pattern come:

### Pattern Saga
- Sequenza di transazioni locali con azioni di compensazione
- Può essere implementato in modo coreografato (basato su eventi) o orchestrato (con un coordinatore)
- Non richiede blocco delle risorse e supporta transazioni di lunga durata

### Pattern Outbox
- Garantisce la pubblicazione affidabile di eventi insieme alle transazioni locali
- Utilizza una tabella "outbox" per memorizzare gli eventi come parte della transazione locale
- Un processo separato pubblica gli eventi dalla tabella outbox, garantendo consegna at-least-once

### Event Sourcing
- Registra le modifiche come sequenza di eventi immutabili
- Lo stato attuale è derivato applicando tutti gli eventi in ordine
- Naturalmente adatto a sistemi distribuiti e architetture event-driven

### CQRS (Command Query Responsibility Segregation)
- Separa le operazioni di lettura e scrittura
- Spesso usato insieme all'Event Sourcing
- Migliora scalabilità e flessibilità

### Eventual Consistency
- Accetta che la consistenza possa essere raggiunta nel tempo, non immediatamente
- Semplifica significativamente l'architettura distribuita
- Più adatto alla maggior parte dei casi d'uso nei microservizi

## Conclusioni

Il Two-Phase Commit rimane uno strumento importante nell'arsenale delle transazioni distribuite, particolarmente adatto a sistemi che richiedono consistenza forte e immediata. Tuttavia, le sue limitazioni in termini di prestazioni, scalabilità e resilienza lo rendono poco adatto all'architettura a microservizi moderna.

Nell'era dei microservizi, pattern alternativi come Saga, Outbox ed Event Sourcing offrono un migliore equilibrio tra consistenza, disponibilità e tolleranza alle partizioni (seguendo il teorema CAP), consentendo di costruire sistemi distribuiti più scalabili e resilienti.

La scelta tra 2PC e questi pattern alternativi dovrebbe basarsi su un'attenta valutazione dei requisiti specifici dell'applicazione, considerando fattori come la tolleranza alle inconsistenze temporanee, i requisiti di scalabilità e la necessità di resilienza.
