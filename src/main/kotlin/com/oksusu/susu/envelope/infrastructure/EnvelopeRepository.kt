package com.oksusu.susu.envelope.infrastructure

import com.oksusu.susu.envelope.domain.Envelope
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EnvelopeRepository : JpaRepository<Envelope, Long>
