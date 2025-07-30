# PDF Templates Directory

This directory should contain PDF AcroForm templates with named form fields.

## Required Template

Place a PDF file named `contract-template.pdf` in this directory. The PDF should be an AcroForm with the following named fields:

- `firstName` - Text field for the signer's first name
- `lastName` - Text field for the signer's last name
- `email` - Text field for the signer's email address
- `date` - Text field for the signature date
- `signature` - Signature field for the electronic signature

## Creating an AcroForm PDF

You can create an AcroForm PDF using:
- Adobe Acrobat Pro
- LibreOffice Writer (export as PDF with form fields)
- Online PDF form creators

Make sure the form fields are properly named and configured as fillable fields.

## Example Template Structure

The template should be a standard PDF document with form fields that can be filled programmatically using Apache PDFBox.