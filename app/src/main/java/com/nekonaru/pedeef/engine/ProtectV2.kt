package com.nekonaru.pedeef.engine

import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission
import com.tom_roush.pdfbox.pdmodel.encryption.InvalidPasswordException
import com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy

/** Encrypts with distinct user and owner passwords and explicit permissions. */
fun protectWithPermissions(
  bytes: ByteArray,
  openPassword: String,
  ownerPassword: String,
  print: Boolean,
  copy: Boolean,
  modify: Boolean,
  annotate: Boolean,
  fillForms: Boolean,
  assemble: Boolean,
): ByteArray? {
  if (openPassword.isEmpty()) fail("Enter an open password.")
  val doc = try {
    PDDocument.load(bytes)
  } catch (_: InvalidPasswordException) {
    return null
  }
  doc.use {
    if (it.isEncrypted) return null
    val perms = AccessPermission().apply {
      setCanPrint(print)
      setCanExtractContent(copy)
      setCanModify(modify)
      setCanModifyAnnotations(annotate)
      setCanFillInForm(fillForms)
      setCanAssembleDocument(assemble)
      setCanExtractForAccessibility(copy)
    }
    it.protect(StandardProtectionPolicy(ownerPassword.ifEmpty { openPassword }, openPassword, perms).apply { encryptionKeyLength = 256 })
    return it.bytes()
  }
}

/** Unlocks with a supplied user or owner password. */
fun unlockWithPassword(bytes: ByteArray, password: String): ByteArray? = unlockBytes(bytes, password)
