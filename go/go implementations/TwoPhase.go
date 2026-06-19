package main

import (
	"fmt"
	"sync"
	"time"
)

type RMState string

const (
	Working   RMState = "working"
	Prepared  RMState = "prepared"
	Committed RMState = "committed"
	Aborted   RMState = "aborted"
)

type TMState string

const (
	Init        TMState = "init"
	TMCommitted TMState = "committed"
	TMAborted   TMState = "aborted"
)

type PreparedMsg struct {
	RMID string
}

type CommitMsg struct{}
type AbortMsg struct{}

type ResourceManager struct {
	ID string

	state RMState

	toTM chan<- PreparedMsg

	fromTM chan any

	wg *sync.WaitGroup
}

func NewRM(
	id string,
	toTM chan<- PreparedMsg,
	wg *sync.WaitGroup,
) *ResourceManager {

	return &ResourceManager{
		ID:     id,
		state:  Working,
		toTM:   toTM,
		fromTM: make(chan any),
		wg:     wg,
	}
}

func (rm *ResourceManager) Run() {
	defer rm.wg.Done()

	/*
	 * Corresponds to RMPrepare.
	 */
	time.Sleep(10 * time.Millisecond)

	rm.state = Prepared

	fmt.Printf("%s prepared\n", rm.ID)

	rm.toTM <- PreparedMsg{
		RMID: rm.ID,
	}

	/*
	 * Corresponds to RMRcvCommitMsg /
	 * RMRcvAbortMsg.
	 */
	msg := <-rm.fromTM

	switch msg.(type) {

	case CommitMsg:
		rm.state = Committed
		fmt.Printf("%s committed\n", rm.ID)

	case AbortMsg:
		rm.state = Aborted
		fmt.Printf("%s aborted\n", rm.ID)
	}
}

type TransactionManager struct {
	state TMState

	rms []*ResourceManager

	prepared map[string]bool

	preparedCh chan PreparedMsg

	wg *sync.WaitGroup
}

func NewTM(
	rms []*ResourceManager,
	preparedCh chan PreparedMsg,
	wg *sync.WaitGroup,
) *TransactionManager {

	return &TransactionManager{
		state:      Init,
		rms:        rms,
		prepared:   make(map[string]bool),
		preparedCh: preparedCh,
		wg:         wg,
	}
}

func (tm *TransactionManager) Run() {
	defer tm.wg.Done()

	/*
	 * Corresponds to repeatedly executing
	 * TMRcvPrepared.
	 */
	for len(tm.prepared) < len(tm.rms) {

		msg := <-tm.preparedCh

		tm.prepared[msg.RMID] = true

		fmt.Printf(
			"TM received Prepared from %s\n",
			msg.RMID,
		)
	}

	/*
	 * Corresponds to TMCommit.
	 */
	tm.state = TMCommitted

	fmt.Println("TM committing")

	for _, rm := range tm.rms {
		rm.fromTM <- CommitMsg{}
	}
}

func main() {

	var wg sync.WaitGroup

	preparedCh := make(chan PreparedMsg)

	rm1 := NewRM("rm1", preparedCh, &wg)
	rm2 := NewRM("rm2", preparedCh, &wg)
	rm3 := NewRM("rm3", preparedCh, &wg)

	rms := []*ResourceManager{
		rm1,
		rm2,
		rm3,
	}

	tm := NewTM(
		rms,
		preparedCh,
		&wg,
	)

	wg.Add(1)
	go tm.Run()

	for _, rm := range rms {
		wg.Add(1)
		go rm.Run()
	}

	wg.Wait()

	fmt.Println()
	fmt.Println("Final state:")
	fmt.Println("TM =", tm.state)

	for _, rm := range rms {
		fmt.Printf(
			"%s = %s\n",
			rm.ID,
			rm.state,
		)
	}
}