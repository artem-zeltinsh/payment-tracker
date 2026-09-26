# Payment tracker
Payment tracker to keep track of the payment history for fraud prevention velocity checks.

## Time and Space Complexity
* N - number of unique cards
* M - number of payment events
* K - avg. number of payment events per card

| Approach                  | Register       | Get count     |  Eviction  |   Space   | Concurrency Mechanism         |
|---------------------------|--------------- |---------------|------------|-----------|-------------------------------|
|CHM + Deque                | O(1) amortized |  O(K)         |  O(M)      | O(N + M)  | CHM level bin locks           |
|CHM + CardHistory + Deque  | O(1) amortized |  O(K)         |  O(M)      | O(N + M)  | CHM + RL per card container   |
|CHM + TreeMap              | O(log K)       |  O(log K + K) |  O(M)      | O(N + M)  | CHM level bin locks           |                 
