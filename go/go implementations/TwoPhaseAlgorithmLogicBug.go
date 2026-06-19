package main

import (
	"flag"
	"fmt"
	"os"
	"strconv"
	"strings"
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

type VoteType int

const (
	PreparedVote VoteType = iota
	PrepareFailedVote
)

type VoteMsg struct {
	RMID string
	Vote VoteType
}

type CommitMsg struct{}
type AbortMsg struct{}

type ResourceManager struct {
	ID string

	state RMState
	
	shouldFail bool

	toTM chan<- VoteMsg

	fromTM chan any

	wg *sync.WaitGroup
}

func NewRM(
	id string,
	shouldFail bool,
	toTM chan<- VoteMsg,
	wg *sync.WaitGroup,
) *ResourceManager {

	return &ResourceManager{
		ID:     id,
		shouldFail: shouldFail,
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

	if rm.shouldFail {

		fmt.Printf("%s prepare FAILED\n", rm.ID)

		rm.toTM <- VoteMsg{
			RMID: rm.ID,
			Vote: PrepareFailedVote,
		}

	} else {

		rm.state = Prepared

		fmt.Printf("%s prepared\n", rm.ID)

		rm.toTM <- VoteMsg{
			RMID: rm.ID,
			Vote: PreparedVote,
		}
	}

	/*
	 * Corresponds to RMRcvCommitMsg /
	 * RMRcvAbortMsg.
	 */
	msg := <-rm.fromTM
	
	if rm.shouldFail {
		rm.state = Aborted;
		return;
	}

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

	abortRequested bool

	voteCh chan VoteMsg

	wg *sync.WaitGroup
}

func NewTM(
	rms []*ResourceManager,
	voteCh chan VoteMsg,
	wg *sync.WaitGroup,
) *TransactionManager {

	return &TransactionManager{
		state:      Init,
		rms:        rms,
		prepared:   make(map[string]bool),
		voteCh: voteCh,
		wg:         wg,
	}
}

func (tm *TransactionManager) Run() {
	defer tm.wg.Done()

	for {
		msg := <-tm.voteCh

		switch msg.Vote {

		case PreparedVote:

			tm.prepared[msg.RMID] = true

			fmt.Printf(
				"TM received PREPARED from %s\n",
				msg.RMID,
			)

			// BUG:
			// Equivalent to tmPrepared # {}
			// Commit immediately after the first
			// successful prepare vote.
			tm.state = TMCommitted

			fmt.Println("TM committing")

			for _, rm := range tm.rms {
				rm.fromTM <- CommitMsg{}
			}

			return

		case PrepareFailedVote:

			fmt.Printf(
				"TM received PREPARE_FAILED from %s\n",
				msg.RMID,
			)

			// Ignore failures for now and keep waiting
			// for a PreparedVote.
		}
	}
}

func main() {
	rmArg := flag.String(
		"rm",
		"",
		"comma-separated list of RM failure flags (0 or 1)",
	)

	bufferedChannel := flag.Bool(
		"bufferedChannel",
		false,
		"use a buffered vote channel",
	)

	flag.Parse()

	if *rmArg == "" {
		fmt.Println("usage:")
		fmt.Println("  TwoPhase --rm=1,0,1,0,0,1 [--bufferedChannel]")
		os.Exit(1)
	}

	rmFlags := strings.Split(*rmArg, ",")

	numRM := len(rmFlags)

	var wg sync.WaitGroup

	var voteCh chan VoteMsg

	if *bufferedChannel {
		voteCh = make(chan VoteMsg, numRM)
	} else {
		voteCh = make(chan VoteMsg)
	}

	rms := make([]*ResourceManager, 0, numRM)

	for i, s := range rmFlags {

		s = strings.TrimSpace(s)

		v, err := strconv.Atoi(s)
		if err != nil || (v != 0 && v != 1) {
			fmt.Printf(
				"invalid rm specification '%s' (must be 0 or 1)\n",
				s,
			)
			os.Exit(1)
		}

		shouldFail := (v == 1)

		rm := NewRM(
			fmt.Sprintf("rm%d", i+1),
			shouldFail,
			voteCh,
			&wg,
		)

		rms = append(rms, rm)
	}

	tm := NewTM(
		rms,
		voteCh,
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